package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

// メンバー生成の対象クラスの役割（docs/コンパイラプラグイン設計01.md §5.3 の表の行に対応）。
// 判定は EnumizeHierarchyResolver.membershipOf が行う。名前集合の確定は SUPER_TYPES 中で
// 自身の supertype が未解決のため、raw / 解決済みのどちらの ref からも同じ答えを返す追跡を用いる（docs/コンパイラプラグイン設計01.md §6.1）
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
