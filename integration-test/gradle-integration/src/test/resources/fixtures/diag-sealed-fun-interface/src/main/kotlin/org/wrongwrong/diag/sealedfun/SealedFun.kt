package org.wrongwrong.diag.sealedfun

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-007(V9): sealed fun interface は言語側で不許容（実測）→ 言語エラーへ合流する分岐の確認
@Enumize
sealed fun interface SealedFun {
    fun handle(x: Int): Int
}
