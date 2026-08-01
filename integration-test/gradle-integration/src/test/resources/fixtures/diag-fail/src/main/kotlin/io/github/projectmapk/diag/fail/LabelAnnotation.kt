package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase

// docs/test/ケース04-診断.md DIA-72〜75: @EnumishLabel の付与先・値の不正と、
// 最終 label で判定される LABEL_CLASH の 2 形（変換由来・明示由来）

// --- DIA-72: 付与先が末端でない 4 形（基底・中間 sealed・末端 class の companion = kind・階層外クラス） ---

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

// --- DIA-73: 空白のみの明示 label → INVALID_LABEL（付与先は正当な末端） ---

@Enumize
sealed interface LblBlank {
    @EnumishLabel(" ") data object Spaced : LblBlank
}

// --- DIA-74: ケース変換で初めて衝突する単純名（FooBar / FOO_BAR → FOO_BAR）
//     → 衝突判定は最終 label で行われ、両末端に LABEL_CLASH ---

@Enumize(labelCase = LabelCase.UPPER_SNAKE_CASE)
sealed interface LblCase {
    data class FooBar(val v: Int) : LblCase

    data object FOO_BAR : LblCase
}

// --- DIA-75: 明示 label が他末端の既定 label と衝突 → 両末端に LABEL_CLASH ---

@Enumize
sealed interface LblAlias {
    data object First : LblAlias

    @EnumishLabel("First") data object Second : LblAlias
}
