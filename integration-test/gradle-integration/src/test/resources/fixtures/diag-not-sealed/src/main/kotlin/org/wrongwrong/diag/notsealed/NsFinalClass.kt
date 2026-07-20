package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-004/087: @Enumize を（修飾なし）final class に付与 → ENUMIZE_NOT_SEALED
@Enumize
class NsFinalClass
