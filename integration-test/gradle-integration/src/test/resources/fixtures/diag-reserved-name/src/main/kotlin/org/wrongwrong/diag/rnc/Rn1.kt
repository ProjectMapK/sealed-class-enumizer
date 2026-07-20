package org.wrongwrong.diag.rnc

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-054: 既存ネスト宣言 Enumish（class 亜種） → ENUMIZE_RESERVED_NAME_CLASH
@Enumize
sealed interface Rn1 {
    class Enumish
}
