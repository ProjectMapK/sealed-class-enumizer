package org.wrongwrong.chain

// 実行時観測: 中間 sealed は entries に載らず、再帰展開の結果だけが観測される
fun main() {
    println("OUT:ENTRIES=" + CSI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:KIND=" + (Leaf as CSI).asEnumish().label)
}
