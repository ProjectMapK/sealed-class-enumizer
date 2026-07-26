package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-13/14 用の階層 B
@Enumize
sealed interface MultB {
    data object BL : MultB
}
