package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-087: @Enumize を value class に付与 → ENUMIZE_NOT_SEALED（value class は言語上 sealed になれない）
@Enumize
@JvmInline
value class NsValueClass(val v: Int)
