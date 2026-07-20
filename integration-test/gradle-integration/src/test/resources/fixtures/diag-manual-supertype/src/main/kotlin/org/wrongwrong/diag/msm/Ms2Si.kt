package org.wrongwrong.diag.msm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-051: 間接継承（Ms2MyBase 経由）でも判定対象 → ENUMIZE_MANUAL_SUPERTYPE_MISMATCH（報告位置=基底宣言）
@Enumize
sealed interface Ms2Si : Ms2MyBase {
    data object L2 : Ms2Si
}
