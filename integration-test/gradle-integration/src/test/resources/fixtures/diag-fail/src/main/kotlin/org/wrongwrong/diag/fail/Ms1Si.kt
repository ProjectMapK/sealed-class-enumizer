package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-46: 基底の Enumized<別型> 直接継承 → ENUMIZE_MANUAL_SUPERTYPE_MISMATCH
@Enumize
sealed interface Ms1Si : Enumized<MsWrong> {
    data object L1 : Ms1Si
}
