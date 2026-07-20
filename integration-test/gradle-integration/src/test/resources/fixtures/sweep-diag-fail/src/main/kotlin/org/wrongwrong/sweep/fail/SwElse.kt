package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-062 用: internal 基底 + 基底内ネスト private 末端（生成は成立 = KIND_NOT_ACCESSIBLE 非発火）
@Enumize
internal sealed interface SwElse {
    private data class Hidden(val v: Int) : SwElse

    data object A : SwElse
}
