package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-087: @Enumize を data class に付与 → ENUMIZE_NOT_SEALED
@Enumize
data class NsDataClass(val v: Int)
