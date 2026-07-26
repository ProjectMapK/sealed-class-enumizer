package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-13/14 用の階層 A
@Enumize
sealed interface FamA {
    data object AL : FamA
}
