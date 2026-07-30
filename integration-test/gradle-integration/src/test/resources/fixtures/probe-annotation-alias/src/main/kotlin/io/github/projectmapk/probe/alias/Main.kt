package io.github.projectmapk.probe.alias

// FQN 表記の生成 API 到達を実行時 entries で観測する（import 別名 = AaImport.kt と
// typealias = AaTypealias.kt は ALIASED_ANNOTATION エラーのため、テスト側が除去してから実行する）
fun main() {
    println("OUT:FQ=" + AaFq.Enumish.entries.joinToString(",") { it.label })
}
