package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: private 基底 + internal companion（実効可視性で規則 1）→ 非発火
@Enumize
private sealed interface NmVis4 {
    class L(val v: Int) : NmVis4 {
        internal companion object
    }
}
