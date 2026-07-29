package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-48: 階層メンバー（末端）の Enumized<別型> でも MSM
data class MsLeaf(val v: Int) : MsLeafSi, Enumized<MsWrong> {
    companion object
}
