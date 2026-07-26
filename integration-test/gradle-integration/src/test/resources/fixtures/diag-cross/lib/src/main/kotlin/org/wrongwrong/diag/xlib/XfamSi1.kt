package org.wrongwrong.diag.xlib

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-22 用の階層 1
@Enumize
sealed interface XfamSi1 {
    interface LeafC : XfamSi1
}
