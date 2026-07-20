package org.wrongwrong.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

// TestKit フィクスチャの展開と GradleRunner の起動を集約するハーネス（docs/テストケース管理.md
// Gradle TestKit 方針）。フィクスチャは src/test/resources/fixtures/<name> に置き、
// settings.gradle.kts 等のプレースホルダをコピー時に置換する:
// - %%PARENT_BUILD%%    … 親ビルド（sealed-class-enumizer ルート）の絶対パス（スラッシュ区切り）
// - %%BUILD_CACHE_DIR%% … テスト毎に隔離したローカルビルドキャッシュのディレクトリ
object TestKitHarness {
    private val parentBuild: String =
        requireNotNull(System.getProperty("enumizer.parentBuild")) {
            "システムプロパティ enumizer.parentBuild が未設定（gradle-integration/build.gradle.kts が注入する)"
        }.replace('\\', '/')

    private val fixturesRoot: Path =
        Path.of(requireNotNull(javaClass.classLoader.getResource("fixtures")) {
            "src/test/resources/fixtures が見つからない"
        }.toURI())

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
                            path.readText()
                                .replace("%%PARENT_BUILD%%", parentBuild)
                                .replace("%%BUILD_CACHE_DIR%%", cacheDir.toString().replace('\\', '/'))
                        )
                    }
                    else -> {
                        target.parent.createDirectories()
                        path.copyTo(target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
    }

    private fun isTextFile(path: Path): Boolean {
        val name = path.fileName.toString()
        return name.endsWith(".kts") || name.endsWith(".kt") || name.endsWith(".properties") ||
            name.endsWith(".java") || name.endsWith(".txt")
    }

    fun build(projectDir: Path, vararg arguments: String): BuildResult =
        runner(projectDir, arguments).build()

    fun buildAndFail(projectDir: Path, vararg arguments: String): BuildResult =
        runner(projectDir, arguments).buildAndFail()

    private fun runner(projectDir: Path, arguments: Array<out String>): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(listOf(*arguments) + "--stacktrace")
            .forwardOutput()

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
