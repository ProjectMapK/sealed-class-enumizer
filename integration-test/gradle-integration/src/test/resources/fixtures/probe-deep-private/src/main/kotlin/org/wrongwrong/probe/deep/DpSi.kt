package org.wrongwrong.probe.deep

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-68 用の基底
@Enumize
sealed interface DpSi {
    data object Open : DpSi
}
