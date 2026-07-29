package io.github.projectmapk.diag.ok

// docs/test/ケース04-診断.md DIA-70: 階層外クラスからの open 具象継承（companion object : OkCiBase()）
// → MC 非発火（クラス supertype の検査対象は final のみ・open は生成 override が勝つ）
class OkCi(val v: Int) : OkCiSi {
    companion object : OkCiBase()
}
