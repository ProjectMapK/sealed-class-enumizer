package org.wrongwrong.diag.mppf

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-10 用の基底（commonMain）
@Enumize
sealed interface MppC {
    data object CLeaf : MppC
}
