package io.github.projectmapk.maven

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

// Maven フィクスチャの展開と Maven の起動を集約するハーネス（docs/test/フィクスチャ構成.md §6）。
// フィクスチャは src/test/resources/fixtures/<name> に置き、テキストのプレースホルダを展開時に置換する:
// - %%ENUMIZER_VERSION%% … 本プラグインのフル版（<KotlinVersion>-<自版>）
// - %%KOTLIN_VERSION%% … フィクスチャが適用する kotlin-maven-plugin の版
// 版の値は親ビルドを正とし、maven-integration の test タスクが systemProperty で渡す。
// プラグイン一式は publishToMavenLocal の出力を隔離リポジトリへ複製して解決する
object MavenHarness {
    private val mavenHome: Path = Path.of(requiredSystemProperty("enumizer.mavenHome"))

    private val mavenLocalRepo: Path = Path.of(requiredSystemProperty("enumizer.mavenLocalRepo"))

    private val publishedRepo: Path = Path.of(requiredSystemProperty("enumizer.publishedRepo"))

    private val fixtureWorkRoot: Path = Path.of(requiredSystemProperty("enumizer.fixtureWorkRoot"))

    private val enumizerVersion: String = requiredSystemProperty("enumizer.version")

    private val kotlinVersion: String = requiredSystemProperty("enumizer.kotlinVersion")

    private val fixturesRoot: Path =
        Path.of(
            requireNotNull(javaClass.classLoader.getResource("fixtures")) {
                    "src/test/resources/fixtures が見つからない"
                }
                .toURI()
        )

    private val workDirCounter = AtomicInteger()

    private fun requiredSystemProperty(key: String): String =
        requireNotNull(System.getProperty(key)) {
            "システムプロパティ $key が未設定（maven-integration の test タスクが設定する）"
        }

    // フィクスチャ一式を一意な作業ディレクトリへ展開する（1 テスト 1 ディレクトリ）
    fun prepareFixture(name: String): Path {
        val source = fixturesRoot.resolve(name)
        require(source.isDirectory()) { "フィクスチャ $name が存在しない: $source" }
        val projectDir =
            fixtureWorkRoot.resolve("$name-${workDirCounter.getAndIncrement()}").createDirectories()
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val target = projectDir.resolve(path.relativeTo(source).toString())
                when {
                    path.isDirectory() -> target.createDirectories()
                    isTextFile(path) -> {
                        target.parent.createDirectories()
                        target.writeText(expandTextFixture(path.readText()))
                    }
                    else -> {
                        target.parent.createDirectories()
                        path.copyTo(target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
        return projectDir
    }

    private fun expandTextFixture(text: String): String =
        text
            .replace("\r\n", "\n")
            .replace("%%ENUMIZER_VERSION%%", enumizerVersion)
            .replace("%%KOTLIN_VERSION%%", kotlinVersion)

    private fun isTextFile(path: Path): Boolean {
        val name = path.fileName.toString()
        return name.endsWith(".xml") || name.endsWith(".kt") || name.endsWith(".properties")
    }

    // 起動は bin/mvn ではなく classworlds の Launcher を直接叩く（配布物の zip 展開では実行権限が
    // 落ちるため、シェルスクリプト経由にするとホストによって起動できない）。
    // JVM はテストと同じもの（toolchain）を使う
    fun run(projectDir: Path, vararg arguments: String): MavenResult {
        seedLocalRepository()
        val command =
            listOf(
                javaExecutable().toString(),
                "-classpath",
                classWorldsJar().toString(),
                "-Dclassworlds.conf=${mavenHome.resolve("bin").resolve("m2.conf")}",
                "-Dmaven.home=$mavenHome",
                "-Dmaven.multiModuleProjectDirectory=$projectDir",
                "org.codehaus.plexus.classworlds.launcher.Launcher",
                // 対話なし・ダウンロード進捗なしで、出力を照合可能な形に保つ
                "-B",
                "-ntp",
                // ローカルリポジトリはテスト専用の場所へ隔離する（docs/test/フィクスチャ構成.md §6）。
                // 既定の ~/.m2/repository を使うと、Maven が取得する Gradle module metadata を伴わない
                // 成果物がそこへ入り、mavenLocal() を宣言する TestKit フィクスチャの variant 解決を壊す
                "-Dmaven.repo.local=$mavenLocalRepo",
            ) + arguments
        val process =
            ProcessBuilder(command).directory(projectDir.toFile()).redirectErrorStream(true).start()
        // 出力を読み切ってから終了を待つ（先に待つとパイプが埋まって停止する）
        val output = process.inputStream.bufferedReader().use { it.readText() }
        return MavenResult(process.waitFor(), output)
    }

    // 隔離リポジトリへ本プラグインの成果物（publishToMavenLocal の出力）だけを複製する。
    // 第三者依存は Maven が隔離先へ取得するため、~/.m2/repository は読むだけで書き換えない。
    // `_remote.repositories` は複製しない（複製先では取得元が一致せず、再解決を要求されるため）
    private fun seedLocalRepository() {
        val source = publishedRepo.resolve(PUBLISHED_GROUP_PATH)
        require(source.isDirectory()) { "publishToMavenLocal の出力が無い: $source（test タスクが前段で公開する）" }
        val target = mavenLocalRepo.resolve(PUBLISHED_GROUP_PATH)
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val destination = target.resolve(path.relativeTo(source).toString())
                when {
                    path.isDirectory() -> destination.createDirectories()
                    path.fileName.toString() == "_remote.repositories" -> Unit
                    else -> {
                        destination.parent.createDirectories()
                        path.copyTo(destination, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
    }

    private fun javaExecutable(): Path {
        val bin = Path.of(System.getProperty("java.home")).resolve("bin")
        val windows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
        return bin.resolve(if (windows) "java.exe" else "java")
    }

    private fun classWorldsJar(): Path =
        mavenHome.resolve("boot").listDirectoryEntries("plexus-classworlds-*.jar").singleOrNull()
            ?: error("Maven 配布物に plexus-classworlds が 1 つ見つからない: $mavenHome")

    // 複製対象は本プロジェクトの group 配下のみ（フィクスチャの依存指定と同じ座標）
    private const val PUBLISHED_GROUP_PATH: String = "io/github/projectmapk"
}
