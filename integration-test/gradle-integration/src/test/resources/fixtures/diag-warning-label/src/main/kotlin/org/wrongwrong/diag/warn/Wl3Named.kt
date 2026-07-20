package org.wrongwrong.diag.warn

// TC-DIAG-081 用: label の default 実装を持つ階層外 interface
interface Wl3Named {
    val label: String get() = "named"
}
