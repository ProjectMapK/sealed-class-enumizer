package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-75: 明示 label が他末端の既定 label と衝突 → 両末端に LABEL_CLASH
@Enumize
sealed interface LblAlias {
    data object First : LblAlias

    @EnumishLabel("First") data object Second : LblAlias
}
