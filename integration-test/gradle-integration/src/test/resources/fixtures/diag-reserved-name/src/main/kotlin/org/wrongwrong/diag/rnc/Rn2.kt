package org.wrongwrong.diag.rnc

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-054: 既存ネスト宣言 Enumish（object 亜種） → ENUMIZE_RESERVED_NAME_CLASH
@Enumize
sealed interface Rn2 {
    object Enumish
}
