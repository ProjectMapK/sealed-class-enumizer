package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: fun interface（非 sealed）への付与 → ENUMIZE_NOT_SEALED
@Enumize
fun interface NsFunInterface {
    fun handle(x: Int): Int
}
