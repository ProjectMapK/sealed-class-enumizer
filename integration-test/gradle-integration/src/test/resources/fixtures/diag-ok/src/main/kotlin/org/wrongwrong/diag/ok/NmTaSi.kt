package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-52: 型引数別名の手動 Enumized（展開後は厳密一致）→ 注入スキップ
@Enumize
sealed interface NmTaSi : Enumized<NmTaAlias> {
    data object L : NmTaSi
}
