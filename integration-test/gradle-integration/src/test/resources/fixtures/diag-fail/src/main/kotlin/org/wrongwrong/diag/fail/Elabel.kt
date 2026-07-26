package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.EnumizeLabel

// docs/test/ケース04-診断.md DIA-61: @EnumizeLabel は v1 に存在せず未解決（INVALID_LABEL 経路なし）
@Enumize
sealed interface Elabel {
    @EnumizeLabel("Alias")
    data object L : Elabel
}
