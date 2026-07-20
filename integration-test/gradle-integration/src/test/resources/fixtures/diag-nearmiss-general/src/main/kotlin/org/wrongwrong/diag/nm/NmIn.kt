package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-097 用の非 final 末端を持つ基底
@Enumize
sealed interface NmIn {
    abstract class Poly : NmIn {
        companion object
    }
}
