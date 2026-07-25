package org.wrongwrong.diag.xfam

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-094 用の階層 2
@Enumize
sealed interface XfamSi2 {
    interface LeafB : XfamSi2
}
