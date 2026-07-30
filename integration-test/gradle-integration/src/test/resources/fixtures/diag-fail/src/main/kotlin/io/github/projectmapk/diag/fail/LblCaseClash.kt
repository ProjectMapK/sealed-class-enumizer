package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase

// docs/test/ケース04-診断.md DIA-74: ケース変換で初めて衝突する単純名（FooBar / FOO_BAR → FOO_BAR）
// → 衝突判定は最終 label で行われ、両末端に LABEL_CLASH
@Enumize(labelCase = LabelCase.UPPER_SNAKE_CASE)
sealed interface LblCase {
    data class FooBar(val v: Int) : LblCase

    data object FOO_BAR : LblCase
}
