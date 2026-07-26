package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// else 必須の位置依存（internal 基底 + 基底内ネスト private 末端。docs/test/ケース04-診断.md §11 の委譲対象）
@Enumize
internal sealed interface ElseInt {
    private data class Hidden(val v: Int) : ElseInt

    data object A : ElseInt
}
