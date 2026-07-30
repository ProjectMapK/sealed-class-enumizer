package io.github.projectmapk.sealedClassEnumizer.compiler

import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService
@OptIn(ExperimentalCompilerApi::class)
class EnumizeCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(LABEL_CASE_OPTION)

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            LABEL_CASE_OPTION_NAME -> configuration.put(LABEL_CASE_KEY, parseLabelCase(value))
            else ->
                throw CliOptionProcessingException(
                    "Unsupported plugin option: ${option.optionName}"
                )
        }
    }

    companion object {
        // gradle-plugin の getCompilerPluginId と一致していなければならない
        const val PLUGIN_ID: String = "io.github.projectmapk.sealed-class-enumizer"

        // gradle-plugin が SubpluginOption で渡すキーと一致していなければならない。
        // 名前は @Enumize の引数名（EnumizeNames.LABEL_CASE_PARAMETER が compiler-plugin 内の
        // 単一定義）から導出して 3 面（引数・CLI オプション・gradle DSL）で同名に揃える。
        // 値は具体ケースのみ（PROJECT_DEFAULT は受け取らない。docs/概要.md §4）
        val LABEL_CASE_OPTION_NAME: String = EnumizeNames.LABEL_CASE_PARAMETER.asString()

        val LABEL_CASE_KEY: CompilerConfigurationKey<EnumizeLabelCase> =
            CompilerConfigurationKey.create("default label case")

        val LABEL_CASE_OPTION: CliOption =
            CliOption(
                optionName = LABEL_CASE_OPTION_NAME,
                valueDescription = EnumizeLabelCase.entries.joinToString("|") { it.name },
                description =
                    "Project-wide default label case, applied to hierarchies whose '@Enumize' " +
                        "does not specify a concrete 'labelCase'",
                required = false,
                allowMultipleOccurrences = false,
            )

        private fun parseLabelCase(value: String): EnumizeLabelCase =
            EnumizeLabelCase.fromNameOrNull(value)
                ?: throw CliOptionProcessingException(
                    "Unknown value for plugin option '$LABEL_CASE_OPTION_NAME': '$value'. " +
                        "Expected one of: ${EnumizeLabelCase.entries.joinToString { it.name }}"
                )
    }
}
