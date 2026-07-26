package org.wrongwrong.icrounds

// 各ラウンドの実行時観測（entries が stale になっていないことを毎回確認する）
fun main() {
    println("OUT:ENTRIES=" + SI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:DESCRIBE=" + describe(LeafA(1)) + "," + describe(LeafB) + "," + describe(LeafC()))
    println("OUT:CH_ENTRIES=" + CSI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:CH_KIND=" + (ChLeaf as CSI).asEnumish().label)
}
