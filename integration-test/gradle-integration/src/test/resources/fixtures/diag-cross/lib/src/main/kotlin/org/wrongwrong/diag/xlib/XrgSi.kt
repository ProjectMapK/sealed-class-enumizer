package org.wrongwrong.diag.xlib

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-57 用の基底
@Enumize
sealed interface XrgSi {
    data object Ok : XrgSi
}
