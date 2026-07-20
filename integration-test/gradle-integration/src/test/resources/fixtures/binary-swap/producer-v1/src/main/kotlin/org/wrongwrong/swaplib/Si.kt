package org.wrongwrong.swaplib

import org.wrongwrong.sealedClassEnumizer.Enumize

// 旧バイナリ v1（2 末端）。v2 と同一 FQN で末端数のみ異なる（docs/概要.md §7）
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI

    data object Bar : SI
}
