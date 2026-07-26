package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-54: 既存ネスト宣言 Enumish（class 亜種）→ ENUMIZE_RESERVED_NAME_CLASH
@Enumize
sealed interface Rn1 {
    class Enumish
}
