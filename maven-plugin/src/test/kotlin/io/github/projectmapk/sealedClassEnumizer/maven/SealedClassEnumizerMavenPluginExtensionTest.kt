package io.github.projectmapk.sealedClassEnumizer.maven

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.apache.maven.model.Model
import org.apache.maven.model.Plugin
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.AbstractLogger
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.util.xml.Xpp3Dom

// kotlin-maven-plugin の拡張点としての振る舞いの固定。
// 観測はコンパイラへ渡る形（PluginOption.toString = plugin:<ID>:<キー>=<値>）と、
// mojo がオプションの振り分けに使う各フィールドで行う
class SealedClassEnumizerMavenPluginExtensionTest {
    private data class LabelCaseCase(val declared: String?, val expected: String)

    private data class OptionFields(
        val pluginName: String,
        val pluginId: String,
        val key: String,
        val value: String,
    )

    // 警告の発火を観測するための Logger。Plexus は requirement で実装を注入するため、
    // テストでは記録用の実装を差し込む
    private class RecordingLogger : AbstractLogger(Logger.LEVEL_INFO, "test") {
        val warnings: MutableList<String> = mutableListOf()

        override fun debug(message: String, throwable: Throwable?) = Unit

        override fun info(message: String, throwable: Throwable?) = Unit

        override fun warn(message: String, throwable: Throwable?) {
            warnings += message
        }

        override fun error(message: String, throwable: Throwable?) = Unit

        override fun fatalError(message: String, throwable: Throwable?) = Unit

        override fun getChildLogger(name: String): Logger = this
    }

    @Test
    fun labelCaseOptionCarriesTheProjectDefault() {
        val cases =
            listOf(
                // 未指定はコンパイラプラグインの組み込み既定と同値（docs/概要.md §4）
                LabelCaseCase(declared = null, expected = "AS_DECLARED"),
                LabelCaseCase(declared = "UPPER_SNAKE_CASE", expected = "UPPER_SNAKE_CASE"),
                LabelCaseCase(declared = "  KEBAB_CASE  ", expected = "KEBAB_CASE"),
                LabelCaseCase(declared = "   ", expected = "AS_DECLARED"),
            )
        assertEquals(
            cases.map {
                "plugin:io.github.projectmapk.sealed-class-enumizer:labelCase=${it.expected}"
            },
            cases.map {
                extension().getPluginOptions(project(it.declared), execution()).single().toString()
            },
        )
    }

    @Test
    fun pluginOptionIdentifiesTheCompilerPlugin() {
        val option = extension().getPluginOptions(project(), execution()).single()
        assertEquals(
            OptionFields(
                // mojo は <pluginOptions> の接頭辞（= <compilerPlugins> の名前）でオプションを束ねる
                pluginName = "sealed-class-enumizer",
                pluginId = "io.github.projectmapk.sealed-class-enumizer",
                key = "labelCase",
                value = "AS_DECLARED",
            ),
            OptionFields(option.pluginName, option.pluginId, option.key, option.value),
        )
    }

    @Test
    fun executionLevelOptionSuppressesTheProjectDefault() {
        // 直接指定は mojo 自身が渡す。オプションは複数指定を受け付けないため、こちらは何も渡さない
        val declared =
            listOf(
                "sealed-class-enumizer:labelCase=SNAKE_CASE",
                // 他プラグイン向け・他キーの指定は抑止の条件にならない
                "all-open:annotation=example.Ann",
                "sealed-class-enumizer:other=x",
            )
        assertEquals(
            listOf(0, 1, 1),
            declared.map {
                extension()
                    .getPluginOptions(project("UPPER_SNAKE_CASE"), execution(pluginOptions = it))
                    .size
            },
        )
    }

    @Test
    fun unknownLabelCaseIsRejectedWithTheAcceptedValues() {
        val failure =
            assertFailsWith<IllegalArgumentException> {
                extension().getPluginOptions(project("Upper_Snake"), execution())
            }
        assertEquals(
            "Unknown value for the 'sealed-class-enumizer.labelCase' property: 'Upper_Snake'. " +
                "Expected one of: AS_DECLARED, UPPER_SNAKE_CASE, SNAKE_CASE, KEBAB_CASE",
            failure.message,
        )
    }

    @Test
    fun appliesToEveryCompilation() {
        val goals = listOf("compile", "test-compile")
        assertEquals(
            goals.map { true },
            goals.map { extension().isApplicable(project(), execution(goal = it)) },
        )
    }

    @Test
    fun onlyUnsupportedKotlinMinorIsWarned() {
        val logger = RecordingLogger()
        val extension = SealedClassEnumizerMavenPluginExtension().apply { this.logger = logger }
        listOf("2.9.0", SealedClassEnumizerCoordinates.KOTLIN_VERSION).forEach {
            extension.getPluginOptions(project(), execution(kotlinVersion = it))
        }
        assertEquals(
            listOf(
                "sealed-class-enumizer ${SealedClassEnumizerCoordinates.VERSION} targets Kotlin " +
                    "${SealedClassEnumizerCoordinates.KOTLIN_VERSION} and is not verified against " +
                    "Kotlin 2.9.0 applied to fixture. Compilation may fail; use the plugin " +
                    "version matching your Kotlin version."
            ),
            logger.warnings,
        )
    }

    @Test
    fun unknownKotlinVersionIsNotWarned() {
        // 版が読めない構成では警告しない（Logger 未注入でもオプションの組み立ては成立する）
        val options =
            SealedClassEnumizerMavenPluginExtension()
                .getPluginOptions(project(), execution(kotlinVersion = null))
        assertEquals(1, options.size)
    }

    private fun extension(): SealedClassEnumizerMavenPluginExtension =
        SealedClassEnumizerMavenPluginExtension().apply { logger = RecordingLogger() }

    private fun project(labelCase: String? = null): MavenProject {
        val model =
            Model().apply {
                groupId = "io.github.projectmapk.fixtures"
                artifactId = "fixture"
                version = "1.0.0"
            }
        labelCase?.let {
            model.properties.setProperty(
                SealedClassEnumizerMavenPluginExtension.LABEL_CASE_PROPERTY,
                it,
            )
        }
        return MavenProject(model)
    }

    // 適用先の Kotlin 版は kotlin-maven-plugin 自身の版として渡る。
    // pluginOptions は mojo へ渡る実効設定（プラグイン単位・実行単位の合成後）として観測される
    private fun execution(
        goal: String = "compile",
        kotlinVersion: String? = SealedClassEnumizerCoordinates.KOTLIN_VERSION,
        pluginOptions: String? = null,
    ): MojoExecution {
        val plugin =
            Plugin().apply {
                groupId = "org.jetbrains.kotlin"
                artifactId = "kotlin-maven-plugin"
                version = kotlinVersion
            }
        val execution = MojoExecution(plugin, goal, "default-$goal")
        pluginOptions?.let {
            execution.configuration =
                Xpp3Dom("configuration").apply {
                    addChild(
                        Xpp3Dom("pluginOptions").apply {
                            addChild(Xpp3Dom("option").apply { value = it })
                        }
                    )
                }
        }
        return execution
    }
}
