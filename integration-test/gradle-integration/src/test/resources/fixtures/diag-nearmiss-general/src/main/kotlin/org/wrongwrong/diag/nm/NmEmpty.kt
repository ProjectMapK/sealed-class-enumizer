package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-104: 継承者ゼロの空階層 → 完全非発火（entries は空リスト・valueOf は実行時例外）
@Enumize
sealed interface NmEmpty
