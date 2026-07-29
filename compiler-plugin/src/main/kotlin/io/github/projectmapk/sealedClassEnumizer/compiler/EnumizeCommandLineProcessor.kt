package io.github.projectmapk.sealedClassEnumizer.compiler

import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@AutoService
@OptIn(ExperimentalCompilerApi::class)
class EnumizeCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = emptyList()

    companion object {
        // gradle-plugin の getCompilerPluginId と一致していなければならない
        const val PLUGIN_ID: String = "io.github.projectmapk.sealed-class-enumizer"
    }
}
