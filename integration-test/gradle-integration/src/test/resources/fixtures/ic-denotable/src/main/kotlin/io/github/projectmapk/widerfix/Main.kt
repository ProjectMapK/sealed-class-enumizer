package io.github.projectmapk.widerfix

// 同一モジュール内から階層 API を観測する（internal 基底は可視範囲内では通常どおり使える）
fun main() {
    println("OUT:WENTRIES=" + WSI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:WKIND=" + Wide().asEnumish().label)
}
