package org.wrongwrong.kt86121

// 各ラウンドの実行時観測（entries が stale になっていないことを毎回確認する）
fun main() {
    println("OUT:ENTRIES=" + SI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:DESCRIBE=" + describe(LeafA(1)) + "," + describe(LeafB) + "," + describe(LeafC()))
}
