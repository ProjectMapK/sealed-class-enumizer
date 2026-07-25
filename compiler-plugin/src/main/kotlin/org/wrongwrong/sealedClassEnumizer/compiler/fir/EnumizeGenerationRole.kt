package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

// メンバー生成の対象クラスの役割（設計01 §5.3 の表の行に対応）。
// 判定は解決済み supertype による「正式判定」で行う（設計01 §6 の 2 段判定の後段）
sealed interface EnumizeGenerationRole {
    // 生成 Enumish（SI.Enumish）: enumishCompanion / enumizedClass の共変 override を宣言する
    class GeneratedEnumish(val base: FirRegularClassSymbol) : EnumizeGenerationRole

    // 生成 Enumish の Companion: entries / valueOf / valueOfOrNull を宣言する
    class GeneratedEnumishCompanion(val enumish: FirRegularClassSymbol) : EnumizeGenerationRole

    // kind となる companion（既存・生成とも）: label / enumizedClass を宣言する
    class KindCompanion(val leaf: FirRegularClassSymbol, val base: FirRegularClassSymbol) :
        EnumizeGenerationRole

    // 末端 object / data object（kind 自身）: label / enumizedClass / asEnumish を宣言する
    class LeafObject(val base: FirRegularClassSymbol) : EnumizeGenerationRole

    // 末端 class / enum class / interface / fun interface: asEnumish を宣言する
    class LeafClass(val base: FirRegularClassSymbol) : EnumizeGenerationRole
}
