package io.github.projectmapk.icmpp

// 実行時観測（entries が stale になっていないことを毎ラウンド確認する）
fun main() {
    println("OUT:ENTRIES=" + SI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:DESCRIBE=" + describe(LeafA(1)) + "," + describe(LeafB))
}
