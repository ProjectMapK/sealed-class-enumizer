package org.wrongwrong.diag.xfam

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-094 用の階層 1
@Enumize
sealed interface XfamSi1 {
    interface LeafA : XfamSi1
}
