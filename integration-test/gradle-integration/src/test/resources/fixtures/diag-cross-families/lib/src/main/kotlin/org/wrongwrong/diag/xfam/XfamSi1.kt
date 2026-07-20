package org.wrongwrong.diag.xfam

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-094 用の家族 1
@Enumize
sealed interface XfamSi1 {
    interface LeafA : XfamSi1
}
