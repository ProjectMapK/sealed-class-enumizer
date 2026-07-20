package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-004: @Enumize を open class に付与 → ENUMIZE_NOT_SEALED
@Enumize
open class NsOpenClass
