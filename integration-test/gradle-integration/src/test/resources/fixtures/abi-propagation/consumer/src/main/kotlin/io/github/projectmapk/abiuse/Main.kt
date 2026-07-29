package io.github.projectmapk.abiuse

import io.github.projectmapk.abifix.SI

// 跨モジュールの実行時観測（entries は実行時にホルダーで解決される）
fun main() {
    val entries = SI.Enumish.entries
    println("OUT:ENTRIES=" + entries.joinToString(",") { it.label })
    println("OUT:KINDS=" + entries.joinToString(",") { describeKind(it) })
}
