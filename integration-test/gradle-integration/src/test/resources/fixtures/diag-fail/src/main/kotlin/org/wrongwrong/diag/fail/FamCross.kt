package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-13: 兄弟 2 基底の sealed 連鎖交差 → ENUMIZE_MULTIPLE_FAMILIES + 基底 FQN 2 件
data object FamCross : FamA, FamB
