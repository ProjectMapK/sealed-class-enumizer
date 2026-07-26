package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-17 用の基底（末端 interface 2 つ）
@Enumize
sealed interface Amb2 {
    interface LeafA : Amb2

    interface LeafB : Amb2
}
