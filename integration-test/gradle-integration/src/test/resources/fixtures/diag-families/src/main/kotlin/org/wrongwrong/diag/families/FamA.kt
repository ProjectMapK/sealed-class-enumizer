package org.wrongwrong.diag.families

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-013/014 用の階層 A
@Enumize
sealed interface FamA {
    data object AL : FamA
}
