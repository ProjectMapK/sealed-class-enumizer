package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.EnumizeLabel

// docs/test/ケース04-診断.md DIA-61: @EnumizeLabel は v1 に存在せず未解決（INVALID_LABEL 経路なし）
@Enumize
sealed interface Elabel {
    @EnumizeLabel("Alias")
    data object L : Elabel
}
