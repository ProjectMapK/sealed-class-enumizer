package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-42: 末端 class の asEnumish 手動宣言 → MC
class Mc5Leaf : Mc5Si {
    companion object

    override fun asEnumish(): Mc5Si.Enumish = throw UnsupportedOperationException()
}
