package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-03: sealed fun interface は言語 unsupported のみ・NOT_SEALED 不在
@Enumize
sealed fun interface SealedFun {
    fun handle(x: Int): Int
}
