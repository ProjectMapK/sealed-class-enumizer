package org.wrongwrong.diag.xrg

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-071 用の基底
@Enumize
sealed interface XrgSi {
    data object Ok : XrgSi
}
