package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-29: 末端 class の companion（既定名）自身が末端 → ENUMIZE_COMPANION_LEAF_CONFLICT
class ClcHost(val v: Int) : ClcSi {
    companion object : ClcSi
}
