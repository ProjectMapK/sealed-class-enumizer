package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-73: 空白のみの明示 label → INVALID_LABEL（付与先は正当な末端）
@Enumize
sealed interface LblBlank {
    @EnumishLabel(" ") data object Spaced : LblBlank
}
