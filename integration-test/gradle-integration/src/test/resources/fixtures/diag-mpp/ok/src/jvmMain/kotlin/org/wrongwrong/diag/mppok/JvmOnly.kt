package org.wrongwrong.diag.mppok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-08: platform 専用 sealed（actual でない）は
// ON_EXPECT / ON_ACTUAL とも非発火
@Enumize
sealed interface JvmOnly {
    data object L : JvmOnly
}
