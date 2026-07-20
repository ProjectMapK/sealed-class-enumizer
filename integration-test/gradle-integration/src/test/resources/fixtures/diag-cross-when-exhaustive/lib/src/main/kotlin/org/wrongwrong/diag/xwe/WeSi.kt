package org.wrongwrong.diag.xwe

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-078 用の基底（末端追加の編集対象）
@Enumize
sealed interface WeSi {
    data object A : WeSi

    data object B : WeSi
}
