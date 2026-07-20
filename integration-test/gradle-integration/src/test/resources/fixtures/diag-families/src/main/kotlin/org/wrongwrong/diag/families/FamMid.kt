package org.wrongwrong.diag.families

// TC-DIAG-014: 中間 sealed の交差 → ENUMIZE_MULTIPLE_FAMILIES（中間自身に報告）
sealed interface FamMid : FamA, FamB
