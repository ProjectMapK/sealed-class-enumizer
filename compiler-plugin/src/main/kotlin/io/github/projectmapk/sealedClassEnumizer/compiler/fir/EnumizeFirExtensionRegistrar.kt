package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeLabelCase
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.checkers.EnumizeAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

class EnumizeFirExtensionRegistrar(private val defaultLabelCase: EnumizeLabelCase) :
    FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        // 階層照会のセッション単一コンポーネント（各拡張・チェッカーは session.enumizeHierarchyResolver で共有する）。
        // プロジェクト既定の label ケース（CLI オプション由来）はここから注入する
        val resolverFactory: (FirSession) -> FirExtensionSessionComponent = { session ->
            EnumizeHierarchyResolver(session, defaultLabelCase)
        }
        +resolverFactory
        +::EnumizeSupertypeGenerationExtension
        +::EnumizeDeclarationGenerationExtension
        +::EnumizeAdditionalCheckersExtension
    }
}
