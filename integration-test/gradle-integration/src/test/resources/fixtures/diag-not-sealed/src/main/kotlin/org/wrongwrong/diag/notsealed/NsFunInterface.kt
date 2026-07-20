package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-006: @Enumize を fun interface（非 sealed）に付与 → ENUMIZE_NOT_SEALED
@Enumize
fun interface NsFunInterface {
    fun handle(x: Int): Int
}
