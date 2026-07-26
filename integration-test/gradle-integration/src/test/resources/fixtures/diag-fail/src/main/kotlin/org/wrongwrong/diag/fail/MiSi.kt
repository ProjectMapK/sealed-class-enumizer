package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-56 用の基底
@Enumize
sealed interface MiSi {
    data object Ok : MiSi
}
