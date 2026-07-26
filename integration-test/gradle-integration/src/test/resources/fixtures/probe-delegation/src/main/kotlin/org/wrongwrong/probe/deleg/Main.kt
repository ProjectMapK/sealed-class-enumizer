package org.wrongwrong.probe.deleg

// docs/test/ケース04-診断.md DIA-69: 委譲末端の asEnumish が返す kind と entries を実測する
fun main() {
    println("OUT:DEL=" + Del(PSi.Ok).asEnumish().label)
    println("OUT:ENTRIES=" + PSi.Enumish.entries.joinToString(",") { it.label })
}
