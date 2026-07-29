package io.github.projectmapk.icscope

// 各ラウンドの実行時観測（entries の label 列を clean 比較の基準にする。名指しは ScBase / ScA のみ）
fun main() {
    println("OUT:ENTRIES=" + ScBase.Enumish.entries.joinToString(",") { it.label })
    println("OUT:DESCRIBE=" + describe(ScA))
}
