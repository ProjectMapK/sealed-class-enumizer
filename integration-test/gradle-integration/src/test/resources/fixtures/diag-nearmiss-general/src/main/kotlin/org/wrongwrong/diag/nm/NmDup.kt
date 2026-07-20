package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-049: 型引数一致（非ジェネリック）の生成 Enumish supertype の手動重複宣言 → 許容（注入スキップ）
@Enumize
sealed interface NmDup {
    data object L : NmDup, NmDup.Enumish
}
