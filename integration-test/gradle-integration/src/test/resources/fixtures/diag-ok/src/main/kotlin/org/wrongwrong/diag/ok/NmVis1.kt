package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: 基底内ネストの private 末端 → 非発火
@Enumize
sealed interface NmVis1 {
    private data object Hidden : NmVis1

    data object Shown : NmVis1
}
