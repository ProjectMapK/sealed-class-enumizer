package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-29: 名前つき companion が末端でも同様 → COMPANION_LEAF_CONFLICT
class Clc2Host(val v: Int) : Clc2Si {
    companion object Named : Clc2Si
}
