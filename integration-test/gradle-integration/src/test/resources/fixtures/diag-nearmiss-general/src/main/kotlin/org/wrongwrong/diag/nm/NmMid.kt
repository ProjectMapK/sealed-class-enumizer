package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-099 用: 中間 sealed（kind でなく label を持たない）
@Enumize
sealed interface NmMid {
    sealed interface Same : NmMid
}
