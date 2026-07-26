package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-19 用の非 final 末端（単一サブタイプ吸収の対象）
@Enumize
sealed interface NmAbs {
    abstract class Poly2 : NmAbs {
        companion object
    }
}
