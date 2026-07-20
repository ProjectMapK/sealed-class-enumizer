package org.wrongwrong.diag.families

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-015: 末端自身への @Enumize → ENUMIZE_NESTED_IN_HIERARCHY（「アノテーションを外す」案内の個別 ID）
@Enumize
sealed interface FamNested {
    @Enumize
    data object Leaf : FamNested
}
