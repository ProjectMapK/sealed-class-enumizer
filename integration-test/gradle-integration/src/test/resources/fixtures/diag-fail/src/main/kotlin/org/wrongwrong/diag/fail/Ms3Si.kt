package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: E-1 適格 K でも v1 は一律 MSM
@Enumize
sealed interface Ms3Si : Enumized<Ms3Kind> {
    data object L3 : Ms3Si
}
