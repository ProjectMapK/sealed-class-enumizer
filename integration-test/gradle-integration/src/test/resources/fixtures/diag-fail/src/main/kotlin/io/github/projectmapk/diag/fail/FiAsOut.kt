package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-70 用: final 具象 asEnumish（引数なし関数）を持つ階層外クラス
open class FiAsOut {
    fun asEnumish(): FiSi.Enumish = FiAsLeaf
}
