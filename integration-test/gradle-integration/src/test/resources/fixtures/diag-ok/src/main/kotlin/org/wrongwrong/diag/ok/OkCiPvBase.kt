package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-70 用: private の final label を持つ階層外クラス
open class OkCiPvBase {
    private val label: String = "pv"

    fun reveal(): String = label
}
