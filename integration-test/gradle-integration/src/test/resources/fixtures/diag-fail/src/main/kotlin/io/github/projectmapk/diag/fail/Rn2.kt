package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-54: 既存ネスト宣言 Enumish（object 亜種）→ RESERVED_NAME_CLASH
@Enumize
sealed interface Rn2 {
    object Enumish
}
