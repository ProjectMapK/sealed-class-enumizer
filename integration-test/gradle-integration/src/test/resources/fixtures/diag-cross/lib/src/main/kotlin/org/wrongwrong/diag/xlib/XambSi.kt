package org.wrongwrong.diag.xlib

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-18/21 用の基底（末端 interface 2 つ）
@Enumize
sealed interface XambSi {
    interface LeafA : XambSi

    interface LeafB : XambSi
}
