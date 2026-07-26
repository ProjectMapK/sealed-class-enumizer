package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-70: 手動 kind companion による階層外 final label 継承 → MC（companion 位置）
class FiCls(val v: Int) : FiSi {
    companion object : FiOut()
}
