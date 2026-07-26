package org.wrongwrong.diag.tsrc

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-71 用の基底（src/main）
@Enumize
sealed interface TsBase {
    data object T1 : TsBase
}
