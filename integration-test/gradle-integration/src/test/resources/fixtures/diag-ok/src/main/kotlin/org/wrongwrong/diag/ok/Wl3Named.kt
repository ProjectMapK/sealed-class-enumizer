package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-38 用: label の default 実装を持つ階層外 interface
interface Wl3Named {
    val label: String get() = "named"
}
