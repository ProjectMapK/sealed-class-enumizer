package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt

// G 軸: 家族の一意性（ENUMIZE_MULTIPLE_FAMILIES / ENUMIZE_NESTED_IN_HIERARCHY）と kind の一意対応
// （ENUMIZE_AMBIGUOUS_KIND）の同一モジュール内発火系（docs/テストケース管理.md TC-DIAG-013〜020、
// docs/コンパイラプラグイン設計01.md §7.2）
class DiagHierarchyTest : DiagTestBase() {
    private fun families(): String = failOutput("diag-families", "compileKotlin")

    private fun ambiguous(): String = failOutput("diag-ambiguous", "compileKotlin")

    // TC-DIAG-013: 兄弟階層の交差 object X : SI1, SI2
    @Test
    fun siblingFamilyCrossingIsReported() {
        assertDiagnosticAt(
            families(),
            "FamCross.kt",
            4,
            DiagFragments.MULTIPLE_FAMILIES,
            "org.wrongwrong.diag.families.FamA",
            "org.wrongwrong.diag.families.FamB",
        )
    }

    // TC-DIAG-014: 中間 sealed の交差（中間自身と末端の双方に報告）
    @Test
    fun intermediateFamilyCrossingIsReported() {
        assertDiagnosticAt(families(), "FamMid.kt", 4, DiagFragments.MULTIPLE_FAMILIES)
        assertDiagnosticAt(families(), "FamMidLeaf.kt", 4, DiagFragments.MULTIPLE_FAMILIES)
    }

    // TC-DIAG-015: 末端自身への @Enumize（報告位置 = 内側の @Enumize 付き宣言。object 宣言のため
    // 報告行は object キーワード行）
    @Test
    fun annotatedLeafIsReportedAsNested() {
        assertDiagnosticAt(
            families(),
            "FamNested.kt",
            9,
            DiagFragments.NESTED_IN_HIERARCHY,
            "org.wrongwrong.diag.families.FamNested",
        )
    }

    // TC-DIAG-016: 中間 sealed への @Enumize の重ね掛け
    @Test
    fun annotatedIntermediateIsReportedAsNested() {
        assertDiagnosticAt(
            families(),
            "FamNestedMid.kt",
            8,
            DiagFragments.NESTED_IN_HIERARCHY,
            "org.wrongwrong.diag.families.FamNestedMid",
        )
    }

    // TC-DIAG-019: abstract class A : SI と class B : A(), SI（B が 2 つの kind に対応）
    @Test
    fun subtypeOfLeafImplementingBaseIsAmbiguous() {
        assertDiagnosticAt(
            ambiguous(),
            "AmbB.kt",
            4,
            DiagFragments.AMBIGUOUS_KIND,
            "org.wrongwrong.diag.ambiguous.AmbB",
            "org.wrongwrong.diag.ambiguous.AmbA",
        )
    }

    // TC-DIAG-020: 階層内の型が複数の末端 interface を実装
    @Test
    fun implementingMultipleLeafInterfacesIsAmbiguous() {
        assertDiagnosticAt(
            ambiguous(),
            "Amb2C.kt",
            4,
            DiagFragments.AMBIGUOUS_KIND,
            "org.wrongwrong.diag.ambiguous.Amb2.LeafA",
            "org.wrongwrong.diag.ambiguous.Amb2.LeafB",
        )
    }

    // TC-DIAG-017/018/022 は producer-jvm が実証済み（017/018 = 複数の独立階層と非 @Enumize interface の
    // 併用が同居してビルド成功・022 = shape/Triangle の単一末端吸収）。TC-DIAG-095/097/099 の near-miss は
    // DiagNearMissTest、跨モジュール系（021/023/094/096）は DiagCrossModuleTest が担当する
}
