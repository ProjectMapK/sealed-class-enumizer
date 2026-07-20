package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.EnumizeLabel

// TC-DIAG-106: label エイリアス（@EnumizeLabel）は v1 の runtime-api に存在しない
// → 参照自体が未解決になり、ENUMIZE_INVALID_LABEL の発火経路が無いことの固定
@Enumize
sealed interface SwElabel {
    @EnumizeLabel("Alias")
    data object L : SwElabel
}
