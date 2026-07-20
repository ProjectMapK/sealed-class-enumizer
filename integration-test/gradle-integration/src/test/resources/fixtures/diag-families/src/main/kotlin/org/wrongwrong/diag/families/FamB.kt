package org.wrongwrong.diag.families

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-013/014 用の家族 B
@Enumize
sealed interface FamB {
    data object BL : FamB
}
