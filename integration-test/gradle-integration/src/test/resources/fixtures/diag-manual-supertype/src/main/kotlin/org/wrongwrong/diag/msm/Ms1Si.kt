package org.wrongwrong.diag.msm

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-DIAG-050: 基底の手動 Enumized<別の Enumish 型> → ENUMIZE_MANUAL_SUPERTYPE_MISMATCH
@Enumize
sealed interface Ms1Si : Enumized<MsWrong> {
    data object L1 : Ms1Si
}
