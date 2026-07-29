package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-14: 中間 sealed の 2 基底交差 → MULTIPLE_HIERARCHIES（中間自身へ報告）
sealed interface MultMid : MultA, MultB
