package org.wrongwrong.diag.rnc

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-107: 予約名 Enumish の object が末端を兼ねる → ENUMIZE_RESERVED_NAME_CLASH（生成スキップ）
@Enumize
sealed interface Rn4 {
    object Enumish : Rn4
}
