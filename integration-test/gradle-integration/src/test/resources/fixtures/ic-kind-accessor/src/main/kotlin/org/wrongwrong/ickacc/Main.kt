package org.wrongwrong.ickacc

// 決定性観測。OUT: 行を clean / incremental / from-cache の各ビルド間で比較する
fun main() {
    println("OUT:ENTRIES=" + KaSi.Enumish.entries.joinToString(",") { it.label })
    println("OUT:VALUEOF=" + KaSi.Enumish.valueOf("Leaf").label + "," + KaSi.Enumish.valueOf("KaPriv").label)
}
