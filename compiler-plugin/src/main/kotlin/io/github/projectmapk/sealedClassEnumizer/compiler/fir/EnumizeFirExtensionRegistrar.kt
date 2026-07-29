package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import io.github.projectmapk.sealedClassEnumizer.compiler.fir.checkers.EnumizeAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class EnumizeFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        // 階層照会のセッション単一コンポーネント（各拡張・チェッカーは session.enumizeHierarchyResolver で共有する）
        +::EnumizeHierarchyResolver
        +::EnumizeSupertypeGenerationExtension
        +::EnumizeDeclarationGenerationExtension
        +::EnumizeAdditionalCheckersExtension
    }
}
