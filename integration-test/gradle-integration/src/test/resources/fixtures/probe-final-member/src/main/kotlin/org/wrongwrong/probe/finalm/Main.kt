package org.wrongwrong.probe.finalm

// docs/test/ケース04-診断.md DIA-70: final 具象継承末端の label 実測値と entries を観測する
fun main() {
    println("OUT:LABEL=" + FmLeaf.label)
    println("OUT:ENTRIES=" + FmSi.Enumish.entries.joinToString(",") { it.label })
}
