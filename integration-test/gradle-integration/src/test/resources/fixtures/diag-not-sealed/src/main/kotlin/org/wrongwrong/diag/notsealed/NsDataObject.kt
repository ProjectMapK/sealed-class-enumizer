package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-003: @Enumize を data object に付与 → ENUMIZE_NOT_SEALED
@Enumize
data object NsDataObject
