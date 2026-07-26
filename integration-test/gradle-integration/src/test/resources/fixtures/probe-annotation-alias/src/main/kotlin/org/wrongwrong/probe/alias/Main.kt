package org.wrongwrong.probe.alias

// FQN 表記の生成 API 到達を実行時 entries で観測する（import 別名 = AaImport.kt と
// typealias = AaTypealias.kt は ICE のためテスト側が順に除去してから実行する。GATE-02）
fun main() {
    println("OUT:FQ=" + AaFq.Enumish.entries.joinToString(",") { it.label })
}
