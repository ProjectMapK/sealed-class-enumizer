package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-005: @Enumize を非 sealed interface に付与 → ENUMIZE_NOT_SEALED
@Enumize
interface NsInterface
