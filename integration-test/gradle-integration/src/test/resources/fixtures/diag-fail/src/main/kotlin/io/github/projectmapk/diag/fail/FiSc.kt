package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-70 用: final の ctor プロパティ label を自身が持つ階層内基底
// （label 宣言位置には ES 警告 = DIA-37 が併発する）
@Enumize
sealed class FiSc(val label: String)
