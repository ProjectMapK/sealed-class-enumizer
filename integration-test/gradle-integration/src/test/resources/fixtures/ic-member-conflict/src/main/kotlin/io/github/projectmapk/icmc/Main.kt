package io.github.projectmapk.icmc

// docs/test/ケース06-ビルド動態.md BLD-47: open 基準状態では生成 override が勝つことの観測
fun main() {
    println("OUT:LABEL=" + McLeaf.label)
    println("OUT:ENTRIES=" + McSi.Enumish.entries.joinToString(",") { it.label })
}
