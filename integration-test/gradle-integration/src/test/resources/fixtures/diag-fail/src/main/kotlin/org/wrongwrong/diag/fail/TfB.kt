package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-50 用の階層 B（生成 Enumish を型引数として持ち込まれる側）
@Enumize
sealed interface TfB {
    data object Lb : TfB
}
