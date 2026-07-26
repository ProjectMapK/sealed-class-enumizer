package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-43 用の基底（末端 object Real は default 実装の返り値を兼ねる）
@Enumize
sealed interface Mc8Si {
    data object Real : Mc8Si
}
