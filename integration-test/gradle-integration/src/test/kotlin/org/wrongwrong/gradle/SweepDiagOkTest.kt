package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt

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

    // TC-MAN-069: typealias 経由の手動 Enumized<Alias=生成 Enumish>（設計01 §4 の型引数込み照合 = D9X-14）。
    // 照合は展開後の型で行うため厳密一致し、注入スキップで受容される（MISMATCH 非発火）
    @Test
    fun typealiasedManualEnumizedIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }
}
