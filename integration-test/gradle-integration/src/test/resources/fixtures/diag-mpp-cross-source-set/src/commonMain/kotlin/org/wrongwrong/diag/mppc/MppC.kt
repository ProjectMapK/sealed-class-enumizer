package org.wrongwrong.diag.mppc

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-061 用の基底（commonMain）
@Enumize
sealed interface MppC {
    data object CLeaf : MppC
}
