package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-43: 階層外 interface からの asEnumish default 継承 → MC
data class Mc8Leaf(val v: Int) : Mc8Si, Mc8Manual {
    companion object
}
