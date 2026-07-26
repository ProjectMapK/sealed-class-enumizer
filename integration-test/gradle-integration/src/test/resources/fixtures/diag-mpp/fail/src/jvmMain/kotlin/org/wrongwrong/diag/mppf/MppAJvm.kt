package org.wrongwrong.diag.mppf

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-07: actual 宣言への @Enumize → ENUMIZE_ON_ACTUAL 単独
@Enumize
actual sealed interface MppA
