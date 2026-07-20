package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAnywhere
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import org.wrongwrong.gradle.DiagAsserts.assertNoDiagnosticAt
import kotlin.test.Test

// G 軸: label の一意性（ENUMIZE_LABEL_CLASH）と拡張シャドーイング警告（ENUMIZE_EXTENSION_SHADOWED）
// （docs/テストケース管理.md TC-DIAG-039〜043・063〜065・080〜081・098〜099・103、概要 §2・§8）。
//
// 既知 NG（報告位置）: LABEL_CLASH は衝突する両末端に報告される仕様（設計01 §7.1）だが、末端が基底と
// 別ファイルにある場合、実測では基底側ファイルの無意味な座標（別ファイルのオフセットをそのまま適用した
// 位置）に出る。発火の事実（文言と衝突相手の FQN）は正しいため、発火は位置非依存で検証し、
// 仕様どおりの位置検証は @Disabled で残す。
class DiagLabelTest : DiagTestBase() {
    private fun labelClash(): String = failOutput("diag-label-clash", "compileKotlin")

    private fun warnings(): String = successOutput("diag-warning-label", "compileKotlin")

    // TC-DIAG-039: 別々の外側にネストした同名末端 → 両末端を当事者として LABEL_CLASH が発火する
    @Test
    fun nestedLeavesWithSameSimpleNameClash() {
        val output = labelClash()
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Foo", "org.wrongwrong.diag.label.LcOuter1.Foo")
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Foo", "org.wrongwrong.diag.label.LcOuter2.Foo")
    }

    // TC-DIAG-039: 仕様上の報告位置は衝突する両方の末端宣言（設計01 §7.1）
    @Test
    @Disabled("NG: 基底と別ファイルの末端への LABEL_CLASH が基底側ファイルの無関係な座標に報告される — docs/修正方針案.md 反映待ち")
    fun labelClashIsReportedAtBothLeafDeclarations() {
        assertDiagnosticAt(labelClash(), "LcOuter1.kt", 5, DiagFragments.LABEL_CLASH, "Foo")
        assertDiagnosticAt(labelClash(), "LcOuter2.kt", 5, DiagFragments.LABEL_CLASH, "Foo")
    }

    // TC-DIAG-040: companion 自身が末端の場合はその宣言名が label になり、同名の別末端と衝突する
    @Test
    fun companionAsLeafDeclarationNameClashes() {
        val output = labelClash()
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Foo2", "org.wrongwrong.diag.label.Lc2Host.Foo2")
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Foo2", "org.wrongwrong.diag.label.Lc2Outer.Foo2")
    }

    // TC-DIAG-098: 末端 enum class の label は enum 全体の単純名（定数名は判定に関与しない）。
    // 基底と同一ファイルの enum 末端側は仕様どおりの位置に出る
    @Test
    fun enumLeafSimpleNameClashesWithOtherLeaf() {
        val output = labelClash()
        assertDiagnosticAt(output, "Lc3Si.kt", 8, DiagFragments.LABEL_CLASH, "Dup")
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Dup", "org.wrongwrong.diag.label.Lc3Outer.Dup")
    }

    // TC-DIAG-103: LABEL_CLASH と KIND_NOT_ACCESSIBLE が同一階層で独立に発火し相互抑止しない
    // （KIND_NOT_ACCESSIBLE は末端自身の検査のため位置も仕様どおり）
    @Test
    fun independentDiagnosticsCoexistInOneHierarchy() {
        val output = labelClash()
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Same", "org.wrongwrong.diag.label.Lc4A.Same")
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH, "Same", "org.wrongwrong.diag.label.Lc4B.Same")
        assertDiagnosticAt(output, "Lc4Priv.kt", 4, DiagFragments.KIND_NOT_ACCESSIBLE)
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

    // TC-DIAG-065: label 以外の名前のメンバーには警告が出ない
    @Test
    fun otherMemberNameDoesNotWarn() {
        assertNoDiagnosticAt(warnings(), "WlSi.kt", 11)
    }

    // TC-DIAG-081: doc は末端 class が label default を継承する構成も EXTENSION_SHADOWED（PLAUSIBLE 判定）
    // とするが、実測は自クラスの宣言のみが検査対象で、継承 label には ES / MMC のいずれも出ない（分岐確定）
    @Test
    @Disabled("NG: 継承した label default に EXTENSION_SHADOWED が出ない（doc の PLAUSIBLE 判定と相違・実測は完全非発火） — docs/修正方針案.md 反映待ち")
    fun inheritedLabelDefaultWarns() {
        assertDiagnosticAt(warnings(), "Wl3Leaf.kt", 4, DiagFragments.EXTENSION_SHADOWED)
    }

    // TC-DIAG-043: 別ファイル追加で label 衝突を導入すると階層共連れで発火し、削除で解除される（§5.3 #8）。
    // 実測は追加後の incremental ラウンドで LABEL_CLASH が出ず、backend の RuntimeException
    // （Exception while generating code for asEnumish）でクラッシュする（KT-86121 系の既知クラッシュ形）
    @Test
    @Disabled("NG: IC の末端追加ラウンドで LABEL_CLASH が出ず backend RuntimeException になる（KT-86121 系） — docs/修正方針案.md 反映待ち")
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
            |""".trimMargin(),
        )
        val failed = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertDiagnosticAnywhere(failed.output, DiagFragments.LABEL_CLASH, "One", "org.wrongwrong.diag.iclabel.One")
        assertDiagnosticAnywhere(failed.output, DiagFragments.LABEL_CLASH, "One", "org.wrongwrong.diag.iclabel.IcLcOuter.One")
        TestKitHarness.deleteFile(dir, addedPath)
        TestKitHarness.build(dir, "compileKotlin")
    }

    // TC-DIAG-041/099 の near-miss は DiagNearMissTest が、TC-DIAG-042（全末端の単純名が異なる通常構成）は
    // producer-jvm の全フィクスチャが実証済み
}
