package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-42: 末端 class の asEnumish 手動宣言 → MMC
class Mm2Leaf : Mm2Si {
    companion object

    override fun asEnumish(): Mm2Si.Enumish = throw UnsupportedOperationException()
}
