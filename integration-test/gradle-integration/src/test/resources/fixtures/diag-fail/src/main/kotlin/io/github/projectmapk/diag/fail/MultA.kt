package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-13/14 用の階層 A
@Enumize
sealed interface MultA {
    data object AL : MultA
}
