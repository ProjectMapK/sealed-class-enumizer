package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent

// G 軸: 手動宣言と生成の衝突系（ENUMIZE_MANUAL_MEMBER_CONFLICT / ENUMIZE_MANUAL_SUPERTYPE_MISMATCH /
// ENUMIZE_RESERVED_NAME_CLASH / ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY。docs/テストケース管理.md
// TC-DIAG-044〜047・050〜054・068・084・107、設計01 §4・§5.1・§7.2）
class DiagManualMemberTest : DiagTestBase() {
    private fun manualMember(): String = failOutput("diag-manual-member", "compileKotlin")

    private fun manualSupertype(): String = failOutput("diag-manual-supertype", "compileKotlin")

    private fun reservedName(): String = failOutput("diag-reserved-name", "compileKotlin")

    // TC-DIAG-044: kind（末端 object）の label 手動宣言（報告位置 = 手動メンバー）
    @Test
    fun manualLabelOnLeafObjectConflicts() {
        assertDiagnosticAt(
            manualMember(),
            "Mm1Si.kt",
            9,
            DiagFragments.MANUAL_MEMBER_CONFLICT,
            "label",
        )
    }

    // TC-DIAG-045: 末端の asEnumish 手動宣言
    @Test
    fun manualAsEnumishOnLeafConflicts() {
        assertDiagnosticAt(
            manualMember(),
            "Mm2Leaf.kt",
            7,
            DiagFragments.MANUAL_MEMBER_CONFLICT,
            "asEnumish",
        )
    }

    // TC-DIAG-046: kind の enumizedClass 手動宣言
    @Test
    fun manualEnumizedClassOnKindConflicts() {
        assertDiagnosticAt(
            manualMember(),
            "Mm3Si.kt",
            10,
            DiagFragments.MANUAL_MEMBER_CONFLICT,
            "enumizedClass",
        )
    }

    // TC-DIAG-047: 階層外 interface の label default 実装の継承（報告位置 = 当該末端）
    @Test
    fun inheritedLabelDefaultOnLeafObjectConflicts() {
        assertDiagnosticAt(
            manualMember(),
            "Mm4Leaf.kt",
            4,
            DiagFragments.MANUAL_MEMBER_CONFLICT,
            "label",
        )
    }

    // TC-DIAG-050: 基底の手動 Enumized<別の Enumish 型>（型引数込みの照合）
    @Test
    fun directEnumizedSupertypeMismatchIsReported() {
        assertDiagnosticAt(
            manualSupertype(),
            "Ms1Si.kt",
            8,
            DiagFragments.MANUAL_SUPERTYPE_MISMATCH,
            "Ms1Si.Enumish",
        )
    }

    // TC-DIAG-051: 間接継承（interface MyBase : Enumized<別型> 経由）も判定対象（報告位置 = 基底宣言）
    @Test
    fun indirectEnumizedSupertypeMismatchIsReported() {
        assertDiagnosticAt(
            manualSupertype(),
            "Ms2Si.kt",
            6,
            DiagFragments.MANUAL_SUPERTYPE_MISMATCH,
            "Ms2Si.Enumish",
        )
    }

    // TC-DIAG-053: E-1 条件を満たす K でも v1 では MISMATCH のまま（将来拡張の分岐は doc 記録のみ）
    @Test
    fun eligibleCustomKindStillMismatchesInV1() {
        assertDiagnosticAt(
            manualSupertype(),
            "Ms3Si.kt",
            8,
            DiagFragments.MANUAL_SUPERTYPE_MISMATCH,
            "Ms3Si.Enumish",
        )
    }

    // TC-DIAG-054: 既存ネスト宣言 Enumish（class / object / interface の 3 亜種・報告位置 = 既存宣言）
    @Test
    fun existingNestedEnumishClashes() {
        assertDiagnosticAt(reservedName(), "Rn1.kt", 8, DiagFragments.RESERVED_NAME_CLASH)
        assertDiagnosticAt(reservedName(), "Rn2.kt", 8, DiagFragments.RESERVED_NAME_CLASH)
        assertDiagnosticAt(reservedName(), "Rn3.kt", 8, DiagFragments.RESERVED_NAME_CLASH)
    }

    // TC-DIAG-107: 予約名 Enumish の object が末端を兼ねても予約名衝突が優先する
    @Test
    fun nestedEnumishThatIsAlsoLeafClashes() {
        assertDiagnosticAt(reservedName(), "Rn4.kt", 8, DiagFragments.RESERVED_NAME_CLASH)
    }

    // TC-DIAG-068: 階層外クラスによる生成 Enumish の直接実装はエラー（docs/概要.md §8・設計01 §7.2 の現行仕様。
    // テストケース管理.md MI-01 行の「非発火」は V1-(e) 反映前の記述であり doc 側の更新が必要）
    @Test
    fun manualImplementationOutsideHierarchyIsReported() {
        assertDiagnosticAt(
            failOutput("diag-manual-impl", "compileKotlin"),
            "MiRogue.kt",
            7,
            DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY,
            "org.wrongwrong.diag.mi.MiSi",
        )
    }

    // TC-DIAG-084: kind の supertype が toString を抽象再宣言 → 言語エラーへ合流（プラグイン診断は非発火。
    // toString は生成対象外で IR-only 生成も抽象充足には使われない）
    @Test
    fun abstractToStringRedeclarationFailsWithLanguageErrorOnly() {
        val output = failOutput("diag-abstract-tostring", "compileKotlin")
        assertDiagnosticAt(
            output,
            "AtLeaf.kt",
            4,
            DiagFragments.LANG_ABSTRACT_MEMBER_NOT_IMPLEMENTED,
        )
        assertFragmentAbsent(output, DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-DIAG-048/049 相当の手動 toString・TC-DIAG-069 の階層内手動実装は producer-jvm（Labeled.kt /
    // manualimpl/ManualLeaf.kt）が非発火 = ビルド成功を実証済み。TC-DIAG-052/100/049 の supertype 系
    // near-miss と TC-DIAG-083 は DiagNearMissTest が担当する
}
