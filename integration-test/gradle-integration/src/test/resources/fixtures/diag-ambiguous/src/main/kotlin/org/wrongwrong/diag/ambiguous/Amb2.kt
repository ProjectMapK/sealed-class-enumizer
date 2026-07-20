package org.wrongwrong.diag.ambiguous

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-020 用の基底（末端 interface 2 つ）
@Enumize
sealed interface Amb2 {
    interface LeafA : Amb2

    interface LeafB : Amb2
}
