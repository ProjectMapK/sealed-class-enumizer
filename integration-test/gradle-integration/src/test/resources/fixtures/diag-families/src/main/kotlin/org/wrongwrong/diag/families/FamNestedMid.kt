package org.wrongwrong.diag.families

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-016: 中間 sealed への @Enumize の重ね掛け → ENUMIZE_NESTED_IN_HIERARCHY
@Enumize
sealed interface FamNestedMid {
    @Enumize
    sealed interface Mid : FamNestedMid {
        data object MLeaf : Mid
    }
}
