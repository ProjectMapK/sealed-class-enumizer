package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-19 用の非 final 末端を持つ基底
@Enumize
sealed interface NmIn {
    abstract class Poly : NmIn {
        companion object
    }
}
