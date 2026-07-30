package io.github.projectmapk.sealedClassEnumizer.compiler

import com.fueledbycaffeine.autoservice.AutoService
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.EnumizeFirExtensionRegistrar
import io.github.projectmapk.sealedClassEnumizer.compiler.ir.EnumizeIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@AutoService
@OptIn(ExperimentalCompilerApi::class)
class EnumizeCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = EnumizeCommandLineProcessor.PLUGIN_ID

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val defaultLabelCase =
            configuration.get(EnumizeCommandLineProcessor.LABEL_CASE_KEY)
                ?: EnumizeLabelCase.BUILT_IN_DEFAULT
        FirExtensionRegistrarAdapter.registerExtension(
            EnumizeFirExtensionRegistrar(defaultLabelCase)
        )
        IrGenerationExtension.registerExtension(EnumizeIrGenerationExtension(defaultLabelCase))
    }
}
