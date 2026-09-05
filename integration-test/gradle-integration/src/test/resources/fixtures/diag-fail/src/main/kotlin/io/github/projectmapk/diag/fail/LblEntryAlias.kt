package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase.UPPER_SNAKE_CASE as Upper

// docs/test/ケース04-診断.md DIA-74: labelCase を enum エントリの import 別名で指定した形。
// 衝突判定は参照先のエントリで解決した最終 label で行われ、両末端に LABEL_CLASH。
// import 別名は file 単位の解決文脈のため LabelAnnotation.kt から分ける

@Enumize(labelCase = Upper)
sealed interface LblEntryAlias {
    data class FooBar(val v: Int) : LblEntryAlias

    data object FOO_BAR : LblEntryAlias
}
