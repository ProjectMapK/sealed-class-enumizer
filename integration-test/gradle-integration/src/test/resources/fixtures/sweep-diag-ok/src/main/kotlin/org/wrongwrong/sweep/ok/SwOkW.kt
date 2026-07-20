package org.wrongwrong.sweep.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-063（内側）: public 基底 + 基底内ネスト private 末端の kind-when。
// doc の「同一ファイル内の when は else 不要」は言語上不正確（private ネスト宣言の可視範囲は
// 同一ファイルでなく基底本体スコープ）で、基底本体の内側でのみ private kind を名指しでき
// else 不要で網羅する（読み替え。外側 = 別ファイルの else 必要側は sweep-diag-fail の SwElseUse）
@Enumize
sealed interface SwOkW {
    private data object Hidden : SwOkW

    data object Shown : SwOkW

    // 基底本体スコープ内（private kind が可視）での else 無し kind-when
    fun describeSelf(): String = when (asEnumish()) {
        Hidden -> "hidden"
        Shown -> "shown"
    }
}
