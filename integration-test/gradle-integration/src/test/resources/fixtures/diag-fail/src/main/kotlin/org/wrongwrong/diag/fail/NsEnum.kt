package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: enum class への付与 → ENUMIZE_NOT_SEALED
@Enumize
enum class NsEnum { HELP, VERSION }
