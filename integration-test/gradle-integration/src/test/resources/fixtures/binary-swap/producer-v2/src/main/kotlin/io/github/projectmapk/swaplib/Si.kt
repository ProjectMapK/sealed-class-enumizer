package io.github.projectmapk.swaplib

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 新バイナリ v2（3 末端）。v1 に対して末端 Baz を追加した版（docs/概要.md §7）
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI

    data object Bar : SI

    data object Baz : SI
}
