package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-085: 階層メンバー（非 sealed 末端）への @Enumize — NOT_SEALED と NESTED_IN_HIERARCHY の共存境界
@Enumize
sealed interface NsNested {
    @Enumize
    class Leaf : NsNested
}
