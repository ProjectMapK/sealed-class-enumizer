package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-51: 基底が runtime-api の基底 Enumish を手動継承 → MSM 非発火
@Enumize
sealed interface NmBase : Enumish {
    data object L : NmBase
}
