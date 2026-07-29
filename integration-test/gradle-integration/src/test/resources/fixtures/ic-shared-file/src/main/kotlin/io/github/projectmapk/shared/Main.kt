package io.github.projectmapk.shared

// 実行時観測: 両階層の entries を並記する
fun main() {
    println("OUT:SA=" + SA.Enumish.entries.joinToString(",") { it.label })
    println("OUT:SB=" + SB.Enumish.entries.joinToString(",") { it.label })
}
