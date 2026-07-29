package io.github.projectmapk.probe.deep

// entries の観測（多段壁の kind が載るかどうかが固定対象）
fun main() {
    println("OUT:ENTRIES=" + DpSi.Enumish.entries.joinToString(",") { it.label })
}
