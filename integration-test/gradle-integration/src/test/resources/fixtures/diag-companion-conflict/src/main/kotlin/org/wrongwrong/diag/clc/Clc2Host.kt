package org.wrongwrong.diag.clc

// TC-DIAG-108: named companion が同一階層の末端でも同様 → ENUMIZE_COMPANION_LEAF_CONFLICT（既定名以外の亜種）
class Clc2Host(val v: Int) : Clc2Si {
    companion object Named : Clc2Si
}
