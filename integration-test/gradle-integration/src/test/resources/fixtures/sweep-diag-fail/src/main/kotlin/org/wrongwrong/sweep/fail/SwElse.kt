package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-062 用: internal 基底 + 基底内ネスト private 末端（基底本体から参照可能で直接参照 = 生成成立）
@Enumize
internal sealed interface SwElse {
    private data class Hidden(val v: Int) : SwElse

    data object A : SwElse
}
