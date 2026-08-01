package io.github.projectmapk.mpp.fixtures.order

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 中間 sealed の break 順序フィクスチャ（docs/test/ケース05-境界横断.md XMP-36。
// 「中間 sealed の継承者がトップレベル」構成を common ソースセットへ置き、DFS 展開順が
// metadata / 全 platform で決定的に一致することを検証する）
@Enumize sealed interface BreakRoot

// 基底直下のトップレベル末端（FQN 順で BreakMid より先行する）
data object Bbb : BreakRoot

// 非入れ子の中間 sealed（この位置に継承者が入れ子展開される）
sealed interface BreakMid : BreakRoot

// 中間 sealed の継承者（トップレベル配置 = 全体が FQN 序数順にならない break の当事者）
data object Aaa : BreakMid
