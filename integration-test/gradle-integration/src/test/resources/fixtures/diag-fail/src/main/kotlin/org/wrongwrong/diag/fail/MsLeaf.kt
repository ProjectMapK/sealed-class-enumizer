package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-48: 階層メンバー（末端）の Enumized<別型> でも MSM
data class MsLeaf(val v: Int) : MsLeafSi, Enumized<MsWrong> {
    companion object
}
