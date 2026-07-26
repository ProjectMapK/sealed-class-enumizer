package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: value class への付与 → ENUMIZE_NOT_SEALED（value class は sealed 不能）
@Enumize
@JvmInline
value class NsValueClass(val v: Int)
