package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-50 用の階層 A（末端 TfCross が属する側）
@Enumize
sealed interface TfA {
    data object La : TfA
}
