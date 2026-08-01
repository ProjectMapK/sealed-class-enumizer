package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-13/14: 兄弟 2 基底の sealed 連鎖交差 → ENUMIZE_MULTIPLE_HIERARCHIES。
// 末端が直接交差する形（MultCross）と中間 sealed が交差する形（MultMid + MultMidLeaf）を同居させる

@Enumize
sealed interface MultA {
    data object AL : MultA
}

@Enumize
sealed interface MultB {
    data object BL : MultB
}

// DIA-13: 末端の交差 → MH + 基底 FQN 2 件
data object MultCross : MultA, MultB

// DIA-14: 中間 sealed の交差 → MH（中間自身へ報告）
sealed interface MultMid : MultA, MultB

// DIA-14: 交差した中間 sealed の末端側にも報告
data object MultMidLeaf : MultMid
