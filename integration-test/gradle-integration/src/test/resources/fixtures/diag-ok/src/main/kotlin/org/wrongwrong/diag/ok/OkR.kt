package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-55: Enumish 名のプロパティ / 関数は分類子でなく非発火
@Enumize
sealed interface OkR {
    val Enumish: Int get() = 0

    fun Enumish(): Int = 1

    data object L : OkR
}
