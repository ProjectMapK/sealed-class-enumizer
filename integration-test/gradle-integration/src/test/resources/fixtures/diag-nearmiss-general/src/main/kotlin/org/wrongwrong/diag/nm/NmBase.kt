package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-100: 基底が runtime-api の基底 Enumish（非ジェネリック）を手動継承 → MISMATCH 非発火
@Enumize
sealed interface NmBase : Enumish {
    data object L : NmBase
}
