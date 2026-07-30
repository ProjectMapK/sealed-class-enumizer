package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-76 用: compiler-required でないユーザーアノテーションを中間 sealed に
// 付けても SUPER_TYPES 中の基底判定が未解決の型参照を踏まない（回帰: 標準 hasAnnotation による ICE）。
// 末端への @EnumishLabel は INVALID_LABEL の非発火 near-miss を兼ねる
annotation class LblPlain

@Enumize
sealed interface LblOkMid {
    @LblPlain sealed interface Mid : LblOkMid

    @EnumishLabel("mid-leaf") data object MidLeaf : Mid
}
