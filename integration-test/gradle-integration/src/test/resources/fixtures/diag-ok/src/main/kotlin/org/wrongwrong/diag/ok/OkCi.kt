package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-70: 階層外クラスからの open 具象継承（companion object : OkCiBase()）
// → MMC 非発火（検査は interface 限定・生成 override が勝つ）
class OkCi(val v: Int) : OkCiSi {
    companion object : OkCiBase()
}
