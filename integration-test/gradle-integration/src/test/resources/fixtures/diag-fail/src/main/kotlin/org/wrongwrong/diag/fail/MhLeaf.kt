package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-43: 階層外 interface からの asEnumish default 継承 → MMC
data class MhLeaf(val v: Int) : MhSi, MhManual {
    companion object
}
