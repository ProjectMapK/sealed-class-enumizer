package io.github.projectmapk.gradle

import io.github.projectmapk.gradle.DiagAsserts.assertDiagnosticAt
import io.github.projectmapk.gradle.DiagAsserts.assertFragmentAbsent
import io.github.projectmapk.gradle.DiagAsserts.assertFragmentAbsentAt
import io.github.projectmapk.gradle.DiagAsserts.assertNoDiagnosticAt
import kotlin.test.Test

// 単一モジュールの near-miss 非発火・EXTENSION_SHADOWED 警告・raw 追跡表記を diag-ok フィクスチャの
// 1 回の成功ビルドで全件検証する（docs/test/ケース04-診断.md の DIA near-miss 群・
// docs/test/フィクスチャ構成.md §3）。成功ビルド自体がエラー系診断の総体的な非発火を含意し、
// 各メソッドは当該断片の不在（またはファイル毎の無診断）で境界を固定する。
// フィクスチャは 1 ファイル = 1 診断ケースであり、ES 警告の行照合はその配置に対する実測値である
class DiagSingleOkTest : DiagTestBase() {
    private fun ok(): String = successOutput("diag-ok", "compileKotlin")

    // docs/test/ケース04-診断.md DIA-05: 正値と下限境界（sealed class / interface・継承者ゼロ・
    // 型パラメータ付き基底 / 末端）は全診断非発火
    @Test
    fun wellFormedAndBoundaryHierarchiesReportNothing() {
        val output = ok()
        assertFragmentAbsentAt(output, "WellFormed.kt", "e: ")
        assertFragmentAbsentAt(output, "WellFormed.kt", "w: ")
    }

    // docs/test/ケース04-診断.md DIA-05: @Enumize 付き local class は全入口で除外され無診断素通り
    @Test
    fun localClassIsSkippedSilently() {
        val output = ok()
        assertFragmentAbsentAt(output, "OkLocal.kt", "e: ")
        assertFragmentAbsentAt(output, "OkLocal.kt", "w: ")
        assertFragmentAbsent(output, DiagFragments.NOT_SEALED)
    }

    // docs/test/ケース04-診断.md DIA-16: 併用・ダイヤモンド・独立 2 階層は MH / NESTED 非発火
    @Test
    fun hierarchyNearMissesDoNotReport() {
        val output = ok()
        assertFragmentAbsent(output, DiagFragments.MULTIPLE_HIERARCHIES)
        assertFragmentAbsent(output, DiagFragments.NESTED_IN_HIERARCHY)
    }

    // docs/test/ケース04-診断.md DIA-19: 単一末端サブタイプ（inner 含む）は吸収され AK / IL 非発火
    @Test
    fun singleLeafSubtypesAreAbsorbed() {
        val output = ok()
        assertFragmentAbsent(output, DiagFragments.AMBIGUOUS_KIND)
        assertFragmentAbsent(output, DiagFragments.INNER_LEAF)
    }

    // docs/test/ケース04-診断.md DIA-24: 非 inner のネスト末端は非発火
    @Test
    fun nonInnerNestedLeafDoesNotReport() {
        val output = ok()
        assertFragmentAbsentAt(output, "AbsorptionNearMiss.kt", "e: ")
        assertFragmentAbsentAt(output, "AbsorptionNearMiss.kt", "w: ")
    }

    // docs/test/ケース04-診断.md DIA-26: 3 段規則の成立形（規則 1 / 2・広い末端 object・private 基底・
    // 基底ネスト private 末端）は KTD 非発火（C2 の分岐網羅）
    @Test
    fun visibilityRuleNearMissesDoNotReport() {
        assertFragmentAbsent(ok(), DiagFragments.KIND_TYPE_NOT_DENOTABLE)
    }

    // docs/test/ケース04-診断.md DIA-30: 階層外 companion 単独末端・SI 非実装の通常 companion は非発火
    @Test
    fun companionNearMissesDoNotConflict() {
        assertFragmentAbsent(ok(), DiagFragments.COMPANION_LEAF_CONFLICT)
    }

    // docs/test/ケース04-診断.md DIA-31: raw 追跡スコープ順の表記（外側ネスト・import 別名・
    // 同一 pkg 直接名・FQN）+ enum 末端・末端 object で候補判定成立 = kind 成立
    // （star import 直接形は sealed の同一 pkg 制約で言語上不能・別 pkg typealias 経由 = DIA-32 のみ成立）
    @Test
    fun everyRawSupertypeNotationYieldsAKind() {
        val output = ok()
        assertFragmentAbsent(output, DiagFragments.LANG_ABSTRACT_MEMBER_NOT_IMPLEMENTED)
        listOf("NmAl.kt", "NmAlImported.kt").forEach { file ->
            assertFragmentAbsentAt(output, file, "e: ")
        }
    }

    // docs/test/ケース04-診断.md DIA-32: typealias 表記（同一 pkg・別 pkg 明示 / star import）と
    // 明示 companion 併用 2 形で候補判定成立
    @Test
    fun typealiasNotationsYieldAKind() {
        val output = ok()
        listOf("NmAl.kt", "NmAlImported.kt", "NmFarNoc.kt", "NmStarNoc.kt").forEach { file ->
            assertFragmentAbsentAt(output, file, "e: ")
        }
    }

    // docs/test/ケース04-診断.md DIA-36: kind companion 名・中間名・enum 定数名・階層外サブタイプ名は
    // label 判定に関与しない
    @Test
    fun labelNearMissesDoNotClash() {
        assertFragmentAbsent(ok(), DiagFragments.LABEL_CLASH)
    }

    // docs/test/ケース04-診断.md DIA-37: 可視 label メンバーの宣言（末端 class 本体・基底自身・
    // 関数形 fun label()・階層内手動実装 leaf の Enumish 由来 override）で ES 警告
    @Test
    fun declaredLabelMemberWarns() {
        val output = ok()
        assertDiagnosticAt(output, "ExtensionShadowed.kt", 13, DiagFragments.EXTENSION_SHADOWED)
        assertDiagnosticAt(output, "ExtensionShadowed.kt", 18, DiagFragments.EXTENSION_SHADOWED)
        assertDiagnosticAt(output, "ExtensionShadowed.kt", 29, DiagFragments.EXTENSION_SHADOWED)
        assertDiagnosticAt(output, "ManualImplAllowed.kt", 20, DiagFragments.EXTENSION_SHADOWED)
    }

    // docs/test/ケース04-診断.md DIA-38: 継承のみの label はクラス位置 + 宣言元 FQN で ES 警告
    @Test
    fun inheritedLabelWarnsWithDeclarationSite() {
        val output = ok()
        assertDiagnosticAt(
            output,
            "ExtensionShadowed.kt",
            44,
            DiagFragments.EXTENSION_SHADOWED,
            "Wl3Named",
        )
        assertDiagnosticAt(
            output,
            "ExtensionShadowed.kt",
            33,
            DiagFragments.EXTENSION_SHADOWED,
            "Wl2Si",
        )
    }

    // docs/test/ケース04-診断.md DIA-39: 非発火 3 形（label 以外・末端 object の継承・private label。
    // 階層内手動実装 leaf の Enumish 由来 label は除外されず ES 発火 = DIA-37）
    @Test
    fun shadowingExclusionsDoNotWarn() {
        val output = ok()
        listOf(15, 31, 50).forEach { line ->
            assertNoDiagnosticAt(output, "ExtensionShadowed.kt", line)
        }
    }

    // docs/test/ケース04-診断.md DIA-44: toString は手動宣言・継承具象とも MC 対象外
    @Test
    fun toStringIsExemptFromMemberConflict() {
        assertFragmentAbsentAt(ok(), "ToStringExempt.kt", DiagFragments.MEMBER_CONFLICT)
    }

    // docs/test/ケース04-診断.md DIA-70: クラス継承の非発火側 — open 具象（companion 経由・末端 object
    // 直接）は生成 override が勝ち、宣言種別交差（final 関数 label）と private は衝突しない
    @Test
    fun classInheritanceNearMissesDoNotConflict() {
        val output = ok()
        assertFragmentAbsentAt(output, "ClassInheritance.kt", "e: ")
        assertFragmentAbsentAt(output, "ClassInheritance.kt", "w: ")
    }

    // docs/test/ケース04-診断.md DIA-45: 生成先違い（末端本体 enumizedClass・kind companion の
    // 手動 asEnumish・enumishCompanion override）は非発火
    @Test
    fun nonGeneratedTargetsDoNotConflict() {
        assertFragmentAbsentAt(ok(), "NonGeneratedTargets.kt", DiagFragments.MEMBER_CONFLICT)
    }

    // docs/test/ケース04-診断.md DIA-51: 型引数一致の手動宣言（直接・間接・末端冗長宣言）は注入スキップ
    @Test
    fun matchingManualSupertypesAreSkipped() {
        val output = ok()
        assertFragmentAbsent(output, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
        assertFragmentAbsent(output, DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
    }

    // docs/test/ケース04-診断.md DIA-52: 照合は typealias 展開後（型引数別名・頭別名の先解決配置・
    // 末端 Enumish 別名・同一ファイル別名の raw 追跡）
    @Test
    fun typealiasedSupertypesMatchAfterExpansion() {
        val output = ok()
        listOf("TypealiasMatch.kt", "NmTlSame.kt").forEach { file ->
            assertFragmentAbsentAt(output, file, "e: ")
        }
        assertFragmentAbsent(output, DiagFragments.LANG_PLATFORM_DECLARATION_CLASH)
    }

    // docs/test/ケース04-診断.md DIA-55: 非 @Enumize 基底のネスト Enumish・別名のネスト宣言・
    // Enumish 名のプロパティ / 関数は非発火
    @Test
    fun reservedNameNearMissesDoNotClash() {
        assertFragmentAbsent(ok(), DiagFragments.RESERVED_NAME_CLASH)
    }

    // docs/test/ケース04-診断.md DIA-58: 階層内手動実装・基底 Enumish 実装・kind companion 免除の
    // 成立側（companion の明示 `: Enumish`・メンバーは生成が充足）は許容
    @Test
    fun hierarchyInternalAndBaseEnumishImplsAreAllowed() {
        val output = ok()
        assertFragmentAbsent(output, DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
        assertFragmentAbsentAt(output, "ManualImplAllowed.kt", "e: ")
    }

    // docs/test/ケース04-診断.md DIA-76: 中間 sealed への任意のユーザーアノテーションは基底判定を
    // 壊さず（回帰: 未解決の型参照による ICE）、末端への正当な @EnumishLabel も無診断
    @Test
    fun annotatedMidAndValidEnumishLabelReportNothing() {
        val output = ok()
        assertFragmentAbsentAt(output, "LblOkMid.kt", "e: ")
        assertFragmentAbsentAt(output, "LblOkMid.kt", "w: ")
    }
}
