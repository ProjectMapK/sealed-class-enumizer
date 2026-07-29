package io.github.projectmapk.diag.mppok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-08: platform 専用 sealed（actual でない）は
// ON_EXPECT / ON_ACTUAL とも非発火
@Enumize
sealed interface JvmOnly {
    data object L : JvmOnly
}
