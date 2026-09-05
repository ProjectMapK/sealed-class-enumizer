package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.name.Name

// 生成対象メンバーと同じ宣言種別（名前付き関数・プロパティ）の名前。同名の手動宣言の検出
// （生成の抑止と ENUMIZE_MEMBER_CONFLICT の報告）はこの読み取りで宣言種別を揃え、
// 名前を持たない宣言種別やネストクラスは対象にしない
val FirDeclaration.callableNameOrNull: Name?
    get() =
        when (this) {
            is FirNamedFunction,
            is FirProperty -> symbol.name
            else -> null
        }
