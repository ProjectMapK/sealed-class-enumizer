package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: fun interface（非 sealed）への付与 → ENUMIZE_NOT_SEALED
@Enumize
fun interface NsFunInterface {
    fun handle(x: Int): Int
}
