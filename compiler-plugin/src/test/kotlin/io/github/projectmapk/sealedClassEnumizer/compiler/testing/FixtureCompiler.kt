package io.github.projectmapk.sealedClassEnumizer.compiler.testing

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeCommandLineProcessor
import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeLabelCase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.walk
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services

// テスト JVM 内でのフィクスチャのコンパイル（docs/test/テスト戦略.md §4）。
// コンパイラプラグインは配布物と同じ jar をプラグインクラスパスへ渡して読み込ませるため、
// 登録経路（ServiceLoader）と設定の受け渡しは実ビルドと同一である。
// 同一のフィクスチャ・単位列は結果を共有する（フィクスチャは読み取り専用の入力であり、
// コンパイルは同一入力に同一出力を返すため、テスト JVM の全体で 1 回に集約できる）
object FixtureCompiler {
    // Gradle の test タスクが渡す配布物 jar とフィクスチャの展開先
    private val pluginJar: String = requiredProperty("enumizer.pluginJar")

    private val fixtureRoot: Path = Path.of(requiredProperty("enumizer.fixtureRoot"))

    private val workRoot: Path = Path.of(requiredProperty("enumizer.compileWorkRoot"))

    // フィクスチャのコンパイルクラスパス。テスト実行時のクラスパスには kotlin-stdlib と
    // runtime-api が含まれ、実ビルドのフィクスチャが解決するものと同じ座標である
    private val baseClasspath: String = System.getProperty("java.class.path")

    private val cache = mutableMapOf<String, List<CompileResult>>()

    // 単一モジュールのフィクスチャをコンパイルして失敗を要求し、その出力を返す
    fun failOutput(fixture: String): String = single(fixture).also { check(!it.succeeded) }.output

    // 単一モジュールのフィクスチャをコンパイルして成功を要求し、その出力を返す（非発火 near-miss 用）
    fun successOutput(fixture: String): String =
        single(fixture).also { check(it.succeeded) { it.output } }.output

    // 単位列を宣言順にコンパイルする。後段は前段までの出力をクラスパスに持ち、
    // friend 指定の単位は直前の出力を friend paths として受け取る
    @Synchronized
    fun compile(fixture: String, units: List<FixtureCompilation>): List<CompileResult> =
        cache.getOrPut(fixture + "|" + units.joinToString()) { runUnits(fixture, units) }

    private fun single(fixture: String): CompileResult =
        compile(fixture, listOf(FixtureCompilation.main())).single()

    private fun runUnits(fixture: String, units: List<FixtureCompilation>): List<CompileResult> {
        val results = mutableListOf<CompileResult>()
        units.forEachIndexed { index, unit ->
            val previous = results.lastOrNull()?.classesDir
            results +=
                runUnit(
                    unit = unit,
                    fixture = fixture,
                    sources = sourcesOf(fixture, unit),
                    classesDir = workRoot.resolve(fixture).resolve(index.toString()),
                    upstream = results.map(CompileResult::classesDir),
                    friendPath = previous?.takeIf { unit.friendOfPrevious },
                )
        }
        return results
    }

    @OptIn(ExperimentalPathApi::class)
    private fun sourcesOf(fixture: String, unit: FixtureCompilation): List<String> {
        val root = fixtureRoot.resolve(fixture).resolve(unit.sourceRoot)
        val sources =
            root
                .walk()
                .filter { it.extension == "kt" }
                .map { it.toAbsolutePath().toString() }
                .toList()
        check(sources.isNotEmpty()) { "フィクスチャ $fixture の ${unit.sourceRoot} に .kt が無い" }
        return sources
    }

    private fun runUnit(
        unit: FixtureCompilation,
        fixture: String,
        sources: List<String>,
        classesDir: Path,
        upstream: List<Path>,
        friendPath: Path?,
    ): CompileResult {
        // コンパイラは出力先の既存クラスを消さないため、前回実行の残骸が
        // 後段のクラスパスへ紛れ込まないよう作り直す
        classesDir.toFile().deleteRecursively()
        classesDir.createDirectories()
        val messages = ByteArrayOutputStream()
        val arguments =
            K2JVMCompilerArguments().apply {
                freeArgs = sources
                destination = classesDir.toAbsolutePath().toString()
                classpath =
                    (listOf(baseClasspath) + upstream.map { it.toAbsolutePath().toString() })
                        .joinToString(File.pathSeparator)
                friendPaths =
                    friendPath?.let { arrayOf(it.toAbsolutePath().toString()) } ?: emptyArray()
                // stdlib はテストクラスパス側から与えるため、既定の同梱解決は行わせない
                noStdlib = true
                noReflect = true
                moduleName = moduleNameOf(fixture, unit)
                pluginClasspaths = if (unit.pluginApplied) arrayOf(pluginJar) else emptyArray()
                // プロジェクト既定の label ケースは Gradle 側が常に具体値で渡すため、
                // フィクスチャのコンパイルでも組み込み既定を明示して同じ状態にする
                pluginOptions =
                    if (unit.pluginApplied) arrayOf(defaultLabelCaseOption()) else emptyArray()
            }
        // 診断は Gradle 経由と同じレンダリング（`e: file:///...:<行>:<列> <メッセージ>`）で受ける
        val collector =
            PrintingMessageCollector(
                PrintStream(messages, true, Charsets.UTF_8),
                MessageRenderer.GRADLE_STYLE,
                true,
            )
        val exitCode = K2JVMCompiler().exec(collector, Services.EMPTY, arguments)
        return CompileResult(exitCode, messages.toString(Charsets.UTF_8), classesDir)
    }

    // -module-name は Gradle と同じ粒度で与える（モジュール = ソースルートの持ち主・
    // test コンパイレーションは接尾辞付き）
    private fun moduleNameOf(fixture: String, unit: FixtureCompilation): String {
        val owner = unit.sourceRoot.substringBefore("/src/", fixture)
        return if (unit.friendOfPrevious) "_test" else owner
    }

    private fun defaultLabelCaseOption(): String =
        "plugin:${EnumizeCommandLineProcessor.PLUGIN_ID}:" +
            "${EnumizeCommandLineProcessor.LABEL_CASE_OPTION_NAME}=" +
            EnumizeLabelCase.BUILT_IN_DEFAULT.name

    private fun requiredProperty(key: String): String =
        System.getProperty(key) ?: error("システムプロパティ $key が設定されていない")
}
