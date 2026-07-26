package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-60: MMC / MSM 発火時も形状生成は継続する
// （発火宣言に隣接する生成 API 参照が未解決にならない = このファイルには診断が出ない）
fun shapeProbeMm(): Int = Mm1Si.Enumish.entries.size

fun shapeProbeMs(): Int = Ms1Si.Enumish.entries.size
