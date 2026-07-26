package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-05: 正値の sealed interface 階層（全診断非発火）
@Enumize
sealed interface OkSi {
    data object A : OkSi

    data class B(val v: Int) : OkSi
}
