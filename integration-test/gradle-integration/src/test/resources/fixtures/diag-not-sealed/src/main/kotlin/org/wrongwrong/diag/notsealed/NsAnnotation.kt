package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-002: @Enumize を annotation class に付与 → ENUMIZE_NOT_SEALED
@Enumize
annotation class NsAnnotation
