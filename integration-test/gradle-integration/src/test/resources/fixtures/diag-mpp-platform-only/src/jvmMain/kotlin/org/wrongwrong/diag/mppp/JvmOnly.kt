package org.wrongwrong.diag.mppp

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-012: platform 専用 sealed（actual でない）への @Enumize → ON_ACTUAL/ON_EXPECT とも非発火
@Enumize
sealed interface JvmOnly {
    data object L : JvmOnly
}
