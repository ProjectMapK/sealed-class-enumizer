package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-44: kind companion の手動 toString 宣言も MC 非発火（toString は対象外）
class NmTsMan(val v: Int) : NmTs {
    companion object {
        override fun toString(): String = "manual!"
    }
}
