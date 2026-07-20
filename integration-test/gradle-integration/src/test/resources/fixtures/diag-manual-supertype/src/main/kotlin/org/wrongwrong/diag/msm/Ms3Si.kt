package org.wrongwrong.diag.msm

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-DIAG-053: v1 では E-1 未実装のため MANUAL_SUPERTYPE_MISMATCH のまま（将来拡張時は K 採用へ分岐）
@Enumize
sealed interface Ms3Si : Enumized<Ms3Kind> {
    data object L3 : Ms3Si
}
