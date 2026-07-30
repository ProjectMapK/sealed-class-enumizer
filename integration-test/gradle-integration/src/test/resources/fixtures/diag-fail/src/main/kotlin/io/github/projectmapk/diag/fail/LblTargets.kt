package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-72: @EnumishLabel の付与先が末端でない 4 形
// （基底・中間 sealed・末端 class の companion = kind・階層外クラス）→ INVALID_LABEL
@EnumishLabel("base")
@Enumize
sealed interface LblT {
    @EnumishLabel("mid") sealed interface Mid : LblT

    data object MidLeaf : Mid

    class Leaf(val v: Int) : LblT {
        @EnumishLabel("companion") companion object
    }
}

@EnumishLabel("outside") class LblOutside
