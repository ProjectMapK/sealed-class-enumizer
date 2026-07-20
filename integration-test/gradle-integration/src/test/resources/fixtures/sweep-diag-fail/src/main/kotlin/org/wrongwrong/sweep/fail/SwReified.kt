package org.wrongwrong.sweep.fail

// TC-MPP-037: reified ヘルパ（enumishEntries / enumishValueOf / enumishValueOfOrNull）は
// runtime-api に存在しない（V6 欠番）→ 未解決参照になることの固定
fun swReified(): Any = enumishEntries<SwRogueSi.Enumish>()

fun swReifiedValueOf(): Any = enumishValueOf<SwRogueSi.Enumish>("R1")

fun swReifiedValueOfOrNull(): Any? = enumishValueOfOrNull<SwRogueSi.Enumish>("R1")
