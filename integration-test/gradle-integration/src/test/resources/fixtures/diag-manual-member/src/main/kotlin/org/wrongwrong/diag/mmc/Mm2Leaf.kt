package org.wrongwrong.diag.mmc

// TC-DIAG-045: 末端の asEnumish 手動宣言 → ENUMIZE_MANUAL_MEMBER_CONFLICT
class Mm2Leaf : Mm2Si {
    companion object

    override fun asEnumish(): Mm2Si.Enumish = throw UnsupportedOperationException()
}
