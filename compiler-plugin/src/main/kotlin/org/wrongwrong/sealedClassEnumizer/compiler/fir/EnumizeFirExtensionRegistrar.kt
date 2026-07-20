package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.wrongwrong.sealedClassEnumizer.compiler.fir.checkers.EnumizeAdditionalCheckersExtension

class EnumizeFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        // 階層照会のセッション単一コンポーネント（各拡張・チェッカーは session.enumizeHierarchyResolver で共有する）
        +::EnumizeHierarchyResolver
        +::EnumizeSupertypeGenerationExtension
        +::EnumizeDeclarationGenerationExtension
        +::EnumizeAdditionalCheckersExtension
    }
}
