package io.github.projectmapk.diag.mppf

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-07: actual 宣言への @Enumize → ENUMIZE_ON_ACTUAL 単独
@Enumize
actual sealed interface MppA
