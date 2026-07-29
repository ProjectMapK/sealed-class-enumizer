package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-05: 継承者ゼロの空階層（完全非発火の下限境界）
@Enumize
sealed interface OkEmpty
