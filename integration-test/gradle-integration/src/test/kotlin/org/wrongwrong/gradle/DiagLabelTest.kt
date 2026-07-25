package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt
import org.wrongwrong.gradle.DiagAsserts.assertNoDiagnosticAt

// G 軸: label の一意性（ENUMIZE_LABEL_CLASH）と拡張シャドーイング警告（ENUMIZE_EXTENSION_SHADOWED）
// （docs/テストケース管理.md TC-DIAG-039〜043・063〜065・080〜081・098〜099・103・114〜115、概要 §2・§8）。
// LABEL_CLASH は衝突する両末端の宣言位置に報告される（設計01 §7.1）ため、基底と別ファイルの末端も
// 含めて位置と衝突相手の FQN を検証する。
class DiagLabelTest : DiagTestBase() {
    private fun labelClash(): String = failOutput("diag-label-clash", "compileKotlin")

    private fun warnings(): String = successOutput("diag-warning-label", "compileKotlin")

    // TC-DIAG-039: 別々の外側にネストした同名末端 → 両末端の宣言位置に、互いを衝突相手として発火する
    // （どちらの末端も基底 LcSi とは別ファイル）
    @Test
    fun nestedLeavesWithSameSimpleNameClash() {
        val output = labelClash()
        assertDiagnosticAt(
            output,
            "LcOuter1.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.LcOuter2.Foo",
        )
        assertDiagnosticAt(
            output,
            "LcOuter2.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.LcOuter1.Foo",
        )
    }

    // TC-DIAG-040: companion 自身が末端の場合はその宣言名が label になり、同名の別末端と衝突する
    @Test
    fun companionAsLeafDeclarationNameClashes() {
        val output = labelClash()
        assertDiagnosticAt(
            output,
            "Lc2Host.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.Lc2Outer.Foo2",
        )
        assertDiagnosticAt(
            output,
            "Lc2Outer.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.Lc2Host.Foo2",
        )
    }

    // TC-DIAG-098: 末端 enum class の label は enum 全体の単純名（定数名は判定に関与しない）
    @Test
    fun enumLeafSimpleNameClashesWithOtherLeaf() {
        val output = labelClash()
        assertDiagnosticAt(
            output,
            "Lc3Si.kt",
            8,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.Lc3Outer.Dup",
        )
        assertDiagnosticAt(
            output,
            "Lc3Outer.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.Lc3Si.Dup",
        )
    }

    // TC-DIAG-103: LABEL_CLASH は衝突する両末端に発火し、同一階層の別の末端（Lc4Priv = 参照不能だが
    // IR-only アクセサで load する）の存在に抑止されない（診断の相互非抑止を Lc4Priv 無診断で確認）
    @Test
    fun labelClashReportsBothParticipantsAndIsNotSuppressed() {
        val output = labelClash()
        assertDiagnosticAt(
            output,
            "Lc4A.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.Lc4B.Same",
        )
        assertDiagnosticAt(
            output,
            "Lc4B.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.label.Lc4A.Same",
        )
        assertFragmentAbsentAt(output, "Lc4Priv.kt", "e: ")
    }

    // TC-DIAG-063/080: 末端 class 本体の label メンバーは警告（EXTENSION_SHADOWED）のみで、ビルドは成功し
    // MANUAL_MEMBER_CONFLICT にはならない（label の生成先は kind = companion のため）
    @Test
    fun leafClassLabelMemberWarnsAndDoesNotConflict() {
        val output = warnings()
        assertDiagnosticAt(output, "WlSi.kt", 8, DiagFragments.EXTENSION_SHADOWED)
        assertFragmentAbsent(output, DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-DIAG-064: 基底自身の label 可視メンバーも警告対象（階層メンバーには基底を含む）
    @Test
    fun baseLabelMemberWarns() {
        assertDiagnosticAt(warnings(), "Wl2Si.kt", 8, DiagFragments.EXTENSION_SHADOWED)
    }

    // TC-DIAG-114: 基底の label を継承した末端 class にも警告が出る（宣言元 = 基底をメッセージが示す）。
    // 報告位置は継承先クラスの宣言（継承メンバーには自ファイルの位置が無いため）
    @Test
    fun leafClassInheritingBaseLabelWarns() {
        assertDiagnosticAt(
            warnings(),
            "Wl2Si.kt",
            14,
            DiagFragments.EXTENSION_SHADOWED,
            "org.wrongwrong.diag.warn.Wl2Si",
        )
    }

    // TC-DIAG-115: kind（末端 object）は生成 label が継承した label を override するため非発火
    @Test
    fun leafObjectInheritingBaseLabelDoesNotWarn() {
        assertNoDiagnosticAt(warnings(), "Wl2Si.kt", 11)
    }

    // TC-DIAG-065: label 以外の名前のメンバーには警告が出ない
    @Test
    fun otherMemberNameDoesNotWarn() {
        assertNoDiagnosticAt(warnings(), "WlSi.kt", 11)
    }

    // TC-DIAG-081: 末端 class が階層外 interface から label default を継承する構成も警告対象
    // （生成先は companion のため MMC ではない。MMC-04 = 末端 object との class/object 対比）
    @Test
    fun inheritedLabelDefaultWarns() {
        assertDiagnosticAt(
            warnings(),
            "Wl3Leaf.kt",
            4,
            DiagFragments.EXTENSION_SHADOWED,
            "org.wrongwrong.diag.warn.Wl3Named",
        )
    }

    // TC-DIAG-043: 別ファイル追加で label 衝突を導入すると発火し、削除で解除される（設計00 §5.3 #8）。
    // IC ラウンドは階層の部分集合ビューを持つため、報告はそのラウンドでコンパイルされる末端
    // （= 追加したファイル）のみに出る（衝突相手は既存末端としてメッセージが示す）
    @Test
    fun addingClashingLeafFileTogglesLabelClash() {
        val dir = prepare("diag-ic-label-clash")
        val addedPath = "src/main/kotlin/org/wrongwrong/diag/iclabel/IcLcOuter.kt"
        TestKitHarness.build(dir, "compileKotlin")
        TestKitHarness.writeFile(
            dir,
            addedPath,
            """
            |package org.wrongwrong.diag.iclabel
            |
            |// TC-DIAG-043: 別ファイル追加で label 衝突を導入する側
            |class IcLcOuter {
            |    object One : IcLcSi
            |}
            |"""
                .trimMargin(),
        )
        val failed = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertDiagnosticAt(
            failed.output,
            "IcLcOuter.kt",
            5,
            DiagFragments.LABEL_CLASH,
            "org.wrongwrong.diag.iclabel.One",
        )
        TestKitHarness.deleteFile(dir, addedPath)
        TestKitHarness.build(dir, "compileKotlin")
    }

    // TC-DIAG-041/099 の near-miss は DiagNearMissTest が、TC-DIAG-042（全末端の単純名が異なる通常構成）は
    // producer-jvm の全フィクスチャが実証済み
}
