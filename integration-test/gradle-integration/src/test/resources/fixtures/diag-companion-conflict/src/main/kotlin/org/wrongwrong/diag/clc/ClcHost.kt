package org.wrongwrong.diag.clc

// TC-DIAG-036: 末端 class の companion が同一階層の末端 → ENUMIZE_COMPANION_LEAF_CONFLICT（kind の二重対応）
class ClcHost(val v: Int) : ClcSi {
    companion object : ClcSi
}
