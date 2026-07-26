package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-53: nullable 型引数は Enumized<out T : Enumish> の境界違反（言語エラー）
@Enumize
sealed interface NulArg : Enumized<NulArg.Enumish?> {
    data object L : NulArg
}
