package org.wrongwrong.diag.rnc

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-054: 既存ネスト宣言 Enumish（interface 亜種） → ENUMIZE_RESERVED_NAME_CLASH
@Enumize
sealed interface Rn3 {
    interface Enumish
}
