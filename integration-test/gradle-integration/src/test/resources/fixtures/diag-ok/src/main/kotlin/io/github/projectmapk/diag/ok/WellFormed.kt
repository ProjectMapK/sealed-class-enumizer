package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-05: 正値と下限境界（sealed class / interface・継承者ゼロ・
// 型パラメータ付き基底 / 末端）。本ファイルには診断が一切出ない。
// local class の素通りは OkLocal.kt が担う

// 正値の sealed interface 階層
@Enumize
sealed interface OkSi {
    data object A : OkSi

    data class B(val v: Int) : OkSi
}

// 正値の sealed class 階層（DIA-16 の独立 2 階層の片割れを兼ねる）
@Enumize
sealed class OkSc {
    data object C : OkSc()
}

// 継承者ゼロの空階層（完全非発火の下限境界）
@Enumize
sealed interface OkEmpty

// 型パラメータ付き基底 / 末端
@Enumize
sealed class OkGen<T> {
    class BoxG<T>(val t: T) : OkGen<T>()

    class FixedG : OkGen<Unit>()
}
