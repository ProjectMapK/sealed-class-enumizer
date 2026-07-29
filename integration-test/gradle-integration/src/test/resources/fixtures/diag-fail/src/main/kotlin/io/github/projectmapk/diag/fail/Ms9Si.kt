package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（非 Enumish サブタイプ）。v1 は MSM
// （言語の upper bound violated が併発する形。発火の事実のみ固定する）
@Enumize
sealed interface Ms9Si : Enumized<Ms9K> {
    data object L9 : Ms9Si
}
