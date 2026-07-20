package org.wrongwrong.diag.mi

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-068 用の基底
@Enumize
sealed interface MiSi {
    data object Ok : MiSi
}
