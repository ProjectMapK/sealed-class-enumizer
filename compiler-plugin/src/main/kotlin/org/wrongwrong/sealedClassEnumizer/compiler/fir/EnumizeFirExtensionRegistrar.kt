package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.wrongwrong.sealedClassEnumizer.compiler.fir.checkers.EnumizeAdditionalCheckersExtension

class EnumizeFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::EnumizeSupertypeGenerationExtension
        +::EnumizeDeclarationGenerationExtension
        +::EnumizeAdditionalCheckersExtension
    }
}
