package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt
import kotlin.test.Test

// 残ケース掃討: 非発火 near-miss 側（sweep-diag-ok / sweep-typealias フィクスチャ）。
// いずれもビルド成功そのものが検証の主体で、対象診断の断片が出力に現れないことを併せて確認する
class SweepDiagOkTest : DiagTestBase() {
    private fun sweep(): String = successOutput("sweep-diag-ok", "compileKotlin")

    // TC-MAN-041: Enumish という名前のプロパティ（分類子でない）は予約名衝突しない
    @Test
    fun enumishNamedMemberDoesNotClash() {
        assertFragmentAbsentAt(sweep(), "SwOkR.kt", DiagFragments.RESERVED_NAME_CLASH)
    }

    // TC-MAN-064: 末端 class 本体の enumizedClass 宣言（生成先は companion）は
    // MANUAL_MEMBER_CONFLICT にも EXTENSION_SHADOWED にもならない（label = TC-DIAG-063 との対比）
    @Test
    fun enumizedClassOnLeafBodyDoesNotConflictNorWarn() {
        val output = sweep()
        assertFragmentAbsentAt(output, "SwOkM.kt", DiagFragments.MANUAL_MEMBER_CONFLICT)
        assertFragmentAbsentAt(output, "SwOkM.kt", DiagFragments.EXTENSION_SHADOWED)
    }

    // TC-MAN-037: kind による enumishCompanion の override は許容（同一 object 以外を返せない）
    @Test
    fun enumishCompanionOverrideOnKindIsAllowed() {
        assertFragmentAbsentAt(sweep(), "SwOkC.kt", DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-VIS-063（内側）: 基底内ネスト private 末端は基底本体スコープでのみ名指しでき、
    // その内側の else 無し kind-when は網羅する（doc の「同一ファイル」は言語上「基底本体スコープ」
    // へ読み替え。コンパイル成立自体が検証。外側 = 別ファイルの else 必要側は SweepDiagFailTest）
    @Test
    fun baseBodyScopedKindWhenCoversPrivateNestedLeaf() {
        assertFragmentAbsent(sweep(), "must be exhaustive")
    }

    // TC-MAN-069: doc（設計01 §4 の型引数込み照合 = D9X-14）は typealias 展開後の厳密一致による
    // 注入スキップを期待するが、実測は表層の Enumized<SwTaAlias> が展開されず
    // ENUMIZE_MANUAL_SUPERTYPE_MISMATCH が発火する（docs/テストケース管理.md 残ギャップが
    // プロトタイプ未確認と注記していた点の実測確定 = 新規 NG）
    @Test
    @Disabled("NG: typealias 経由の手動 Enumized<Alias=生成Enumish> が展開後照合されず MANUAL_SUPERTYPE_MISMATCH — docs/修正方針案.md 反映待ち")
    fun typealiasedManualEnumizedIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // TC-MAN-069 の実挙動固定: typealias 経由の手動 Enumized は現状 MISMATCH でコンパイル不能
    // （展開後照合が実装されたらこのテストが fail して検出する）
    @Test
    fun typealiasedManualEnumizedCurrentlyMismatches() {
        val output = failOutput("sweep-typealias", "compileKotlin")
        DiagAsserts.assertDiagnosticAnywhere(output, DiagFragments.MANUAL_SUPERTYPE_MISMATCH, "SwTaAlias")
    }
}
