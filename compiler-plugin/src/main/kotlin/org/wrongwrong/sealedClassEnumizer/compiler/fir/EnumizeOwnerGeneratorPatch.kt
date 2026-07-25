package org.wrongwrong.sealedClassEnumizer.compiler.fir

import java.lang.reflect.Method
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension

// 生成クラス（Enumish）の companion を replaceCompanionObjectSymbol で自前連結すると、
// symbol provider を経由しないため内部属性 ownerGenerator（FirGeneratedDeclarationsUtils.kt・internal）が
// 未設定のままになり、companion のメンバースコープ構築が NPE になる（FirGeneratedScopes.getExtensionsForClass）。
// コンパイラ本体の FirCompanionGenerationTransformer はソース宣言しか走査しないため、生成クラスの companion は
// この属性を自前で刻印するしかない。internal API へのリフレクションは、Kotlin のマイナーバージョン毎に
// アーティファクトを分割する方針（docs/概要.md §7）の下でバージョンに固定される。
internal object EnumizeOwnerGeneratorPatch {
    private val setter: Method? =
        runCatching {
                Class.forName("org.jetbrains.kotlin.fir.FirGeneratedDeclarationsUtilsKt")
                    .methods
                    .firstOrNull { method ->
                        method.name.startsWith("setOwnerGenerator") && method.parameterCount == 2
                    }
            }
            .getOrNull()

    fun stamp(declaration: FirClassLikeDeclaration, extension: FirDeclarationGenerationExtension) {
        setter?.invoke(null, declaration, extension)
    }
}
