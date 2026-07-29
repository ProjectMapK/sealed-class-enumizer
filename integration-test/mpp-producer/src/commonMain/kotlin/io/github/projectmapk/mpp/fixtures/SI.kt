package io.github.projectmapk.mpp.fixtures

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 標準形フィクスチャの MPP 版（docs/test/ケース05-境界横断.md XMP-33・docs/概要.md §1）。
// commonMain に置き、expect / actual でない通常の共通 sealed として全ターゲットで生成される
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI

    data object Bar : SI
}
