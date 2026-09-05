package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-61: reified ヘルパ（enumishEntries / enumishValueOf /
// enumishValueOfOrNull）は v1 に存在しない → 未解決参照
fun probeEntries(): Any = enumishEntries<MiSi.Enumish>()

fun probeValueOf(): Any = enumishValueOf<MiSi.Enumish>("Ok")

fun probeValueOfOrNull(): Any? = enumishValueOfOrNull<MiSi.Enumish>("Ok")
