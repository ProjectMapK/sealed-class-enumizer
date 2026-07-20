package org.wrongwrong.sealedClassEnumizer.compiler

import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.wrongwrong.sealedClassEnumizer.compiler.fir.EnumizeFirExtensionRegistrar
import org.wrongwrong.sealedClassEnumizer.compiler.ir.EnumizeIrGenerationExtension

@AutoService
@OptIn(ExperimentalCompilerApi::class)
class EnumizeCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = EnumizeCommandLineProcessor.PLUGIN_ID

    override val supportsK2: Boolean get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(EnumizeFirExtensionRegistrar())
        IrGenerationExtension.registerExtension(EnumizeIrGenerationExtension())
    }
}
