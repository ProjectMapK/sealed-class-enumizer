package org.wrongwrong.diag.mmc

// TC-DIAG-047 用: label の default 実装を持つ階層外 interface
interface Mm4Named {
    val label: String get() = "named"
}
