package org.wrongwrong.gradle

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

// TestKit フィクスチャの展開と GradleRunner の起動を集約するハーネス（docs/テストケース管理.md
// Gradle TestKit 方針）。フィクスチャは src/test/resources/fixtures/<name> に置き、
// settings.gradle.kts 等のプレースホルダをコピー時に置換する:
// - %%BUILD_CACHE_DIR%% … テスト毎に隔離したローカルビルドキャッシュのディレクトリ
// 併せて全フィクスチャ共通のデーモン設定を gradle.properties へ追記する。
// プラグイン一式はローカル Maven から座標で解決するため、親ビルドの composite 参照は持たない
object TestKitHarness {
    // Gradle の既定（-Xmx512m / MaxMetaspaceSize=384m）はフィクスチャビルドの KGP を載せるには不足する。
    // 全フィクスチャへ同一値を与えることで TestKit のデーモンが 1 種類に揃い、
    // 並行実行するフォークの間でも使い回される（docs/テストケース管理.md Gradle TestKit 方針）。
    // ワーカー数の既定はホストのコア数であり、フィクスチャ（1〜3 プロジェクト）には過大で、
    // 並行実行すると デーモン数 × コア数 だけ多重化されて CPU を奪い合う
    private val daemonSettings =
        listOf(
            "org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=1g",
            "kotlin.daemon.jvmargs=-Xmx2g",
            "org.gradle.workers.max=2",
        )

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
                        target.writeText(
                            path
                                .readText()
                                .replace(
                                    "%%BUILD_CACHE_DIR%%",
                                    cacheDir.toString().replace('\\', '/'),
                                )
                        )
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
    // テストタスク自体が落ちる（docs/テストケース管理.md 並行実行方針）
    private fun runner(projectDir: Path, arguments: Array<out String>): GradleRunner =
        GradleRunner.create()
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
