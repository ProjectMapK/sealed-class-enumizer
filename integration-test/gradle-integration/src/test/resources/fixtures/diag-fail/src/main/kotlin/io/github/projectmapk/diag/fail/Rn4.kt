package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-54: 予約名 Enumish の object が末端兼務でも予約名優先 → RESERVED_NAME_CLASH
@Enumize
sealed interface Rn4 {
    object Enumish : Rn4
}
