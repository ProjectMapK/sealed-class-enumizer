package org.wrongwrong.diag.mppok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-08: 同一ソースセット完結の common 通常 sealed は非発火
@Enumize
sealed interface CommonSi {
    data object C1 : CommonSi
}
