package org.wrongwrong.probe.finalm

// docs/test/ケース04-診断.md DIA-70 用: final 具象 label を持つ階層外クラス（open 修飾なし = final）
open class FmBase {
    val label: String get() = "fixed"
}
