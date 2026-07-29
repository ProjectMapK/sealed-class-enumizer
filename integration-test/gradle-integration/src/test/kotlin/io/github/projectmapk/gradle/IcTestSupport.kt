package io.github.projectmapk.gradle

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import kotlin.streams.asSequence
import org.gradle.testkit.runner.BuildResult

// TestKit テスト群の共通観測ヘルパ（docs/test/フィクスチャ構成.md §4 の検証手段 4 種:
// 実行時 OUT: 行・生成 .class のバイト比較・出力タイムスタンプによる再コンパイル追跡）。
// フィクスチャの展開と GradleRunner の起動は TestKitHarness が持ち、観測系は本オブジェクトへ集約する
object IcTestSupport {
    private const val CLASSES_DIR = "build/classes/kotlin/main"

    // フィクスチャの展開先。@TempDir は Windows のデーモンロックで後始末に失敗しうるため、
    // モジュールの build 配下（clean で回収され、テスト開始時に作り直される場所）へ展開する。
    // 並行実行するフォークの作業ディレクトリに依存しないよう、絶対パスをテストタスクから注入する
    private val fixtureWorkRoot: Path =
        Path.of(
            requireNotNull(System.getProperty("enumizer.fixtureWorkRoot")) {
                "システムプロパティ enumizer.fixtureWorkRoot が未設定（gradle-integration/build.gradle.kts が注入する)"
            }
        )

    fun emptyDir(prefix: String): Path {
        val root = fixtureWorkRoot.createDirectories()
        return Files.createTempDirectory(root, prefix)
    }

    fun prepare(fixture: String, prefix: String): Path {
        val projectDir = emptyDir(prefix)
        TestKitHarness.prepareFixture(fixture, projectDir)
        return projectDir
    }

    // フィクスチャ内 Main が出力する OUT: 行を抽出する（実行時挙動の clean 比較用）
    fun outLines(result: BuildResult): List<String> =
        result.output
            .lineSequence()
            .filter { it.startsWith("OUT:") }
            .map { it.removePrefix("OUT:").trim() }
            .toList()

    // 生成 .class のバイト一致検証用ダイジェスト（キー = クラスディレクトリからの相対パス）
    fun classDigests(projectDir: Path, subProject: String = ""): Map<String, String> =
        walkClasses(projectDir, subProject) { it.sha256() }

    // 再コンパイル追跡用の最終更新時刻（値が変わったファイル = 出力が書き直された = 再コンパイル対象）
    fun classTimes(projectDir: Path, subProject: String = ""): Map<String, Long> =
        walkClasses(projectDir, subProject) { Files.getLastModifiedTime(it).toMillis() }

    // 任意の出力ディレクトリ配下のバイト一致検証用ダイジェスト（MPP の klib / metadata 出力など）
    fun outputDigests(projectDir: Path, relativePath: String): Map<String, String> =
        walkUnder(projectDir.resolve(relativePath)) { it.sha256() }

    // 2 スナップショット間で値が変わった（または出現・消滅した）キーの集合
    fun <T> changedKeys(before: Map<String, T>, after: Map<String, T>): Set<String> =
        (before.keys + after.keys).filterTo(mutableSetOf()) { before[it] != after[it] }

    fun readFile(projectDir: Path, relativePath: String): String =
        projectDir.resolve(relativePath).readText()

    // ファイル改名・宣言移動（編集ケース #5）用: 内容そのままの移動
    fun moveFile(projectDir: Path, fromRelative: String, toRelative: String) {
        val content = readFile(projectDir, fromRelative)
        TestKitHarness.deleteFile(projectDir, fromRelative)
        val target = projectDir.resolve(toRelative)
        target.parent.createDirectories()
        target.writeText(content)
    }

    // relocated ビルド用の複製。ビルド出力とローカルキャッシュは複製しない
    // （settings の buildCache は複製元の絶対パスを指したままになるため、キャッシュは共有される）
    fun copyForRelocation(source: Path, target: Path) {
        Files.walk(source).use { paths ->
            paths
                .asSequence()
                .filter { path -> !isExcludedFromRelocation(source, path) }
                .forEach { path ->
                    val destination = target.resolve(path.relativeTo(source).toString())
                    if (path.isDirectory()) {
                        destination.createDirectories()
                    } else {
                        destination.parent.createDirectories()
                        Files.copy(path, destination)
                    }
                }
        }
    }

    private fun isExcludedFromRelocation(source: Path, path: Path): Boolean =
        path
            .relativeTo(source)
            .map { it.toString() }
            .any { it == "build" || it == ".gradle" || it == "build-cache" || it == ".kotlin" }

    private fun <T> walkClasses(
        projectDir: Path,
        subProject: String,
        value: (Path) -> T,
    ): Map<String, T> {
        val classesRoot =
            if (subProject.isEmpty()) {
                projectDir.resolve(CLASSES_DIR)
            } else {
                projectDir.resolve(subProject).resolve(CLASSES_DIR)
            }
        return walkUnder(classesRoot, value)
    }

    private fun <T> walkUnder(root: Path, value: (Path) -> T): Map<String, T> {
        if (!root.isDirectory()) return emptyMap()
        return Files.walk(root).use { paths ->
            paths
                .asSequence()
                .filter { it.isRegularFile() }
                .associate { it.relativeTo(root).toString().replace('\\', '/') to value(it) }
        }
    }

    private fun Path.sha256(): String =
        MessageDigest.getInstance("SHA-256").digest(readBytes()).joinToString("") {
            "%02x".format(it)
        }
}
