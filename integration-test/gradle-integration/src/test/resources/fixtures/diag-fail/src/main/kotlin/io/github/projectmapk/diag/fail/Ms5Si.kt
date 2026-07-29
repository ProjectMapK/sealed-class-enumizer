package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（sealed）でも v1 は一律 MSM
@Enumize
sealed interface Ms5Si : Enumized<Ms5K> {
    data object L5 : Ms5Si
}
