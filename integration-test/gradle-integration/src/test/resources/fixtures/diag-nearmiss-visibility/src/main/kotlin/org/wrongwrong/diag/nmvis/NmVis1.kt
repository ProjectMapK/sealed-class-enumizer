package org.wrongwrong.diag.nmvis

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-029: 基底内ネストの private 末端は SI.Enumish の内側から参照可能 → 非発火
@Enumize
sealed interface NmVis1 {
    private data object Hidden : NmVis1

    data object Shown : NmVis1
}
