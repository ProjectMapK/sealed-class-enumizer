package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: 基底内ネストの private 末端 → 非発火
@Enumize
sealed interface NmVis1 {
    private data object Hidden : NmVis1

    data object Shown : NmVis1
}
