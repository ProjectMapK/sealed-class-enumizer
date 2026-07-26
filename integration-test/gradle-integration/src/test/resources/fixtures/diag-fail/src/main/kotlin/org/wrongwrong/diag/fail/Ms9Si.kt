package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（非 Enumish サブタイプ）。v1 は MSM
// （言語の upper bound violated が併発する形。発火の事実のみ固定する）
@Enumize
sealed interface Ms9Si : Enumized<Ms9K> {
    data object L9 : Ms9Si
}
