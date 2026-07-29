package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-43 用: label の default 実装を持つ階層外 interface
interface Mc7Named {
    val label: String get() = "named"
}
