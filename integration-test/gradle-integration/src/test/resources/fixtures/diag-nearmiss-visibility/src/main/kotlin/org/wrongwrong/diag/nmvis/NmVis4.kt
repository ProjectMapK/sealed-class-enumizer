package org.wrongwrong.diag.nmvis

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-091: private 基底 + 基底内ネスト末端 + internal companion → 非発火（実効可視性で規則 1）
@Enumize
private sealed interface NmVis4 {
    class L(val v: Int) : NmVis4 {
        internal companion object
    }
}
