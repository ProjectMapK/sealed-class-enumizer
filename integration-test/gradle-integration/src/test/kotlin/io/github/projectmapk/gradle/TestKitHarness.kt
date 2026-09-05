package io.github.projectmapk.gradle

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

// TestKit フィクスチャの展開と GradleRunner の起動を集約するハーネス（docs/test/フィクスチャ構成.md §5 の
// Gradle TestKit 方針）。フィクスチャは src/test/resources/fixtures/<name> に置き、
// settings.gradle.kts 等のプレースホルダをコピー時に置換する:
// - %%BUILD_CACHE_DIR%% … テスト毎に隔離したローカルビルドキャッシュのディレクトリ
// - %%ENUMIZER_VERSION%% … 本プラグインのフル版（<KotlinVersion>-<自版>）
// - %%KOTLIN_VERSION%% … フィクスチャが適用する KGP の版
// 版の値は親ビルドを正とし、gradle-integration の test タスクが systemProperty で渡す。
// 併せて全フィクスチャ共通のデーモン設定を gradle.properties へ追記する。
// プラグイン一式はローカル Maven から依存指定で解決するため、親ビルドの composite 参照は持たない
object TestKitHarness {
    private val enumizerVersion: String = requiredSystemProperty("enumizer.version")

    private val kotlinVersion: String = requiredSystemProperty("enumizer.kotlinVersion")

    private fun requiredSystemProperty(key: String): String =
        requireNotNull(System.getProperty(key)) {
            "システムプロパティ $key が未設定（gradle-integration の test タスクが設定する）"
        }

    // フィクスチャビルドのデーモン設定。全フィクスチャへ同一値を与えることで TestKit のデーモンが
    // 1 種類に揃い、同じホームを使うビルドの間で使い回される（docs/test/フィクスチャ構成.md §5）。
    // メタスペースは Gradle の既定（384m）では KGP を載せるのに不足するため引き上げる。
    // ヒープは同時実行数の算出と同じ値を使う必要があるため、gradle-integration の test タスクから受け取る。
    // ワーカー数の既定はホストのコア数であり、フィクスチャ（1〜4 プロジェクト）には過大で、
    // 並行実行すると 同時ビルド数 × コア数 だけ多重化されて CPU を奪い合う
    private val daemonSettings =
        requiredSystemProperty("enumizer.fixtureDaemonHeapGb").let { heapGb ->
            listOf(
                "org.gradle.jvmargs=-Xmx${heapGb}g -XX:MaxMetaspaceSize=1g",
                "kotlin.daemon.jvmargs=-Xmx${heapGb}g",
                "org.gradle.workers.max=2",
            )
        }

    // TestKit は既定で全フィクスチャビルドが 1 つの Gradle ユーザーホームを共有する。ホームのキャッシュは
    // ビルドを跨いで排他されるため、共有したまま並行実行すると直列化し、直列実行より遅くなる。
    // 従ってビルドを駆動するスレッドへ 1 つずつ専用ホームを割り当てる。依存キャッシュだけは
    // 親ビルドのものを読み取り専用で共有するため（GRADLE_RO_DEP_CACHE をテストタスクが渡す）、
    // ホームが増えても依存の再取得は起きない
    private val testKitHomeRoot: Path = Path.of(requiredSystemProperty("enumizer.testKitHomeRoot"))

    private val slotCounter = AtomicInteger()

    private val slotHome: ThreadLocal<Path> = ThreadLocal.withInitial {
        testKitHomeRoot.resolve("slot-${slotCounter.getAndIncrement()}").createDirectories()
    }

    private val fixturesRoot: Path =
        Path.of(
            requireNotNull(javaClass.classLoader.getResource("fixtures")) {
                    "src/test/resources/fixtures が見つからない"
                }
                .toURI()
        )

    // フィクスチャ一式を projectDir へ展開し、テキストファイルのプレースホルダを置換する
    fun prepareFixture(name: String, projectDir: Path) {
        val source = fixturesRoot.resolve(name)
        require(source.isDirectory()) { "フィクスチャ $name が存在しない: $source" }
        val cacheDir = projectDir.resolve("build-cache").createDirectories()
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val target = projectDir.resolve(path.relativeTo(source).toString())
                when {
                    path.isDirectory() -> target.createDirectories()
                    isTextFile(path) -> {
                        target.parent.createDirectories()
                        target.writeText(expandTextFixture(path.readText(), cacheDir))
                    }
                    else -> {
                        target.parent.createDirectories()
                        path.copyTo(target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
        appendDaemonSettings(projectDir)
    }

    // フィクスチャのテキストは checkout 環境の改行コードに左右されうるため、展開時に LF へ確定させる。
    // replaceInFile 等の下流はテストコード中の LF 文字列と直接照合するので、
    // ここで正規化しないと CRLF の作業ツリーで一致に失敗する。行数は変わらないため、
    // 診断の行番号を検証するフィクスチャの前提は保たれる（docs/test/フィクスチャ構成.md §5）
    private fun expandTextFixture(text: String, cacheDir: Path): String =
        text
            .replace("\r\n", "\n")
            .replace("%%BUILD_CACHE_DIR%%", cacheDir.toString().replace('\\', '/'))
            .replace("%%ENUMIZER_VERSION%%", enumizerVersion)
            .replace("%%KOTLIN_VERSION%%", kotlinVersion)

    // フィクスチャ側の宣言（org.gradle.caching 等）を残したまま共通設定を後置きで追記する
    // （properties は後勝ちのため、同じキーを持つフィクスチャがあれば共通設定が優先される）
    private fun appendDaemonSettings(projectDir: Path) {
        val file = projectDir.resolve("gradle.properties")
        val head = if (file.exists()) file.readText().trimEnd() + "\n" else ""
        file.writeText(head + daemonSettings.joinToString("\n", postfix = "\n"))
    }

    private fun isTextFile(path: Path): Boolean {
        val name = path.fileName.toString()
        return name.endsWith(".kts") ||
            name.endsWith(".kt") ||
            name.endsWith(".properties") ||
            name.endsWith(".java") ||
            name.endsWith(".txt")
    }

    fun build(projectDir: Path, vararg arguments: String): BuildResult =
        runner(projectDir, arguments).build()

    fun buildAndFail(projectDir: Path, vararg arguments: String): BuildResult =
        runner(projectDir, arguments).buildAndFail()

    // forwardOutput() は付けない。検証は BuildResult.output を読むため不要で、フィクスチャビルド
    // 全本数分の出力をテストワーカーの標準出力へ流すと、並行実行時に結果ストリームが壊れて
    // テストタスク自体が落ちる（docs/test/フィクスチャ構成.md §5 の並行実行方針）
    private fun runner(projectDir: Path, arguments: Array<out String>): GradleRunner =
        GradleRunner.create()
            .withTestKitDir(slotHome.get().toFile())
            .withProjectDir(projectDir.toFile())
            .withArguments(listOf(*arguments) + "--stacktrace")

    // フィクスチャ内ファイルの編集（IC 回帰の「ビルド→編集→再ビルド」用）
    fun replaceInFile(projectDir: Path, relativePath: String, old: String, new: String) {
        val file = projectDir.resolve(relativePath)
        val text = file.readText()
        require(text.contains(old)) { "$relativePath に置換対象が見つからない: $old" }
        file.writeText(text.replace(old, new))
    }

    fun writeFile(projectDir: Path, relativePath: String, content: String) {
        val file = projectDir.resolve(relativePath)
        file.parent.createDirectories()
        file.writeText(content)
    }

    fun deleteFile(projectDir: Path, relativePath: String) {
        Files.delete(projectDir.resolve(relativePath))
    }
}
