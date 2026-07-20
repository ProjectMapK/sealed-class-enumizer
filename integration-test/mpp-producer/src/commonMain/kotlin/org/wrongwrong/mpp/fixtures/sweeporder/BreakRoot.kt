package org.wrongwrong.mpp.fixtures.sweeporder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed の break 順序フィクスチャ（docs/テストケース管理.md TC-ORD-063。L-13 相当の
// 「中間 sealed の継承者がトップレベル」構成を common ソースセットへ置き、DFS 展開順が
// metadata / 全 platform で決定的に一致することを検証する）
@Enumize
sealed interface BreakRoot
