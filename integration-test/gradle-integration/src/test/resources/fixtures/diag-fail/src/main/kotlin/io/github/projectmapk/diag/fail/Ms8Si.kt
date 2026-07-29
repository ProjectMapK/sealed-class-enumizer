package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（経路内具象実装）でも v1 は一律 MSM
@Enumize
sealed interface Ms8Si : Enumized<Ms8K> {
    data object L8 : Ms8Si
}
