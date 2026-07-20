package org.wrongwrong.det

// 決定性観測。OUT: 行を clean / incremental / from-cache / relocated の各ビルド間で比較する
fun main() {
    println("OUT:ENTRIES=" + S.Enumish.entries.joinToString(",") { it.label })
    println("OUT:TOSTR=" + listOf(PlainObj, Inherited, S.Custom.Companion, S.Aaa).joinToString(",") { it.toString() })
    println("OUT:NOLABEL=" + probe())
}

private fun probe(): String = try {
    S.Enumish.valueOf("X").label
} catch (e: IllegalArgumentException) {
    "IAE:" + e.message
}
