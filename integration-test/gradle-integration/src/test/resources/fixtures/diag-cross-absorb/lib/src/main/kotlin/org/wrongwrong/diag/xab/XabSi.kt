package org.wrongwrong.diag.xab

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-20 用の基底（非 final 末端）
@Enumize
sealed interface XabSi {
    abstract class Poly : XabSi {
        companion object
    }
}
