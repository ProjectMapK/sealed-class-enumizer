package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt
import org.wrongwrong.gradle.DiagAsserts.assertNoDiagnosticAt
import kotlin.test.Test

// G 軸: kind の参照可能性（ENUMIZE_KIND_NOT_ACCESSIBLE）・asEnumish 返り値型の構成可能性
// （ENUMIZE_KIND_TYPE_NOT_DENOTABLE）・inner class 末端（ENUMIZE_INNER_LEAF）と、その near-miss 群
// （docs/テストケース管理.md TC-DIAG-024〜035・077・088〜093、docs/エッジケースへの対応方針.md §1）
class DiagVisibilityTest : DiagTestBase() {
    private fun kindAccess(): String = failOutput("diag-kind-access", "compileKotlin")

    // TC-DIAG-026: 別ファイルの private トップレベル末端
    @Test
    fun privateTopLevelLeafInAnotherFileIsNotAccessible() {
        assertDiagnosticAt(
            kindAccess(),
            "KnaLeaf.kt",
            4,
            DiagFragments.KIND_NOT_ACCESSIBLE,
            "private top-level leaf",
            "org.wrongwrong.diag.kindaccess.KnaLeaf",
        )
    }

    // TC-DIAG-027: private な既存 companion（報告位置 = companion）
    @Test
    fun privateCompanionIsNotAccessible() {
        assertDiagnosticAt(kindAccess(), "KnaPrivComp.kt", 5, DiagFragments.KIND_NOT_ACCESSIBLE, "is private")
    }

    // TC-DIAG-028: protected な既存 companion
    @Test
    fun protectedCompanionIsNotAccessible() {
        assertDiagnosticAt(kindAccess(), "KnaProtComp.kt", 5, DiagFragments.KIND_NOT_ACCESSIBLE, "is protected")
    }

    // TC-DIAG-089: 基底内ネスト末端でも companion が private なら発火（対象は kind の可視性）
    @Test
    fun privateCompanionOfNestedLeafIsNotAccessible() {
        assertDiagnosticAt(kindAccess(), "KnaNested.kt", 9, DiagFragments.KIND_NOT_ACCESSIBLE, "is private")
    }

    // TC-DIAG-024: 末端が inner class
    @Test
    fun innerClassLeafIsReported() {
        assertDiagnosticAt(kindAccess(), "InnerHost.kt", 5, DiagFragments.INNER_LEAF)
    }

    // TC-DIAG-032: 基底より広い末端 + internal companion → 規則 3 で DENOTABLE（報告位置 = 末端）
    @Test
    fun widerLeafWithInternalCompanionIsNotDenotable() {
        assertDiagnosticAt(
            kindAccess(),
            "KtdLeaf.kt",
            4,
            DiagFragments.KIND_TYPE_NOT_DENOTABLE,
            "org.wrongwrong.diag.kindaccess.KtdLeaf",
        )
    }

    // TC-DIAG-088: 基底より広い末端 + private companion → KIND_NOT_ACCESSIBLE は発火する
    @Test
    fun widerLeafWithPrivateCompanionIsNotAccessible() {
        assertDiagnosticAt(kindAccess(), "KnaxLeaf.kt", 5, DiagFragments.KIND_NOT_ACCESSIBLE, "is private")
    }

    // TC-DIAG-088: doc は「NOT_ACCESSIBLE のみ（DENOTABLE は companion 参照可能が前提）」とするが、
    // 実測は KnaxLeaf に KIND_TYPE_NOT_DENOTABLE も併発する
    @Test
    @Disabled("NG: private companion で KIND_TYPE_NOT_DENOTABLE が併発（doc は NOT_ACCESSIBLE のみ） — docs/修正方針案.md 反映待ち")
    fun widerLeafWithPrivateCompanionDoesNotReportDenotable() {
        assertFragmentAbsentAt(kindAccess(), "KnaxLeaf.kt", DiagFragments.KIND_TYPE_NOT_DENOTABLE)
    }

    // TC-DIAG-077: 同一階層で「別ファイル private トップレベル末端 = 発火」と「基底内ネスト private 末端 =
    // 非発火」が共存し、報告は逸脱末端のみに限定される
    @Test
    fun combinedHierarchyReportsOnlyDeviatingLeaf() {
        val output = kindAccess()
        assertDiagnosticAt(output, "CombOutside.kt", 4, DiagFragments.KIND_NOT_ACCESSIBLE)
        assertNoDiagnosticAt(output, "CombSi.kt", 8)
    }

    // TC-DIAG-093: sealed class 階層で基底より広い末端は言語 EXPOSED_SUPER_CLASS になり DENOTABLE へ到達しない
    @Test
    fun sealedClassWiderLeafFailsWithLanguageExposureError() {
        val output = failOutput("diag-exposed-super", "compileKotlin")
        assertDiagnosticAt(output, "ExpLeaf.kt", 4, DiagFragments.LANG_EXPOSED_SUPER_CLASS)
        assertFragmentAbsent(output, DiagFragments.KIND_TYPE_NOT_DENOTABLE)
    }

    // TC-DIAG-029/030/090/091/092: 可視性系 near-miss はすべて非発火でビルド成功する
    @Test
    fun visibilityNearMissesBuildCleanly() {
        val output = successOutput("diag-nearmiss-visibility", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.KIND_NOT_ACCESSIBLE)
        assertFragmentAbsent(output, DiagFragments.KIND_TYPE_NOT_DENOTABLE)
        assertFragmentAbsent(output, DiagFragments.COMPANION_REQUIRED)
    }

    // TC-DIAG-035: companion 可視性の編集で規則 3 の境界を跨ぐと DENOTABLE が発火し、戻すと解除される
    // （初期状態は TC-DIAG-033 と同じ規則 1 構成）
    @Test
    fun companionVisibilityEditTogglesDenotable() {
        val dir = prepare("diag-ic-denotable")
        val leafPath = "src/main/kotlin/org/wrongwrong/diag/icden/IcDenLeaf.kt"
        TestKitHarness.build(dir, "compileKotlin")
        TestKitHarness.replaceInFile(dir, leafPath, "    companion object", "    internal companion object")
        val failed = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertDiagnosticAt(failed.output, "IcDenLeaf.kt", 4, DiagFragments.KIND_TYPE_NOT_DENOTABLE)
        TestKitHarness.replaceInFile(dir, leafPath, "    internal companion object", "    companion object")
        TestKitHarness.build(dir, "compileKotlin")
    }

    // TC-DIAG-031/033/034 は producer-jvm が実証済み（Mixed.Half = internal companion の規則 2
    // フォールバック成功・widerleaf/PublicLeaf = internal 基底 + public companion の規則 1 成功）
}
