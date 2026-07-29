package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-37/38/39: 基底 label 宣言の ES・継承末端 class の ES・末端 object の除外
@Enumize
sealed interface Wl2Si {
    val label: String get() = "base"

    data object L : Wl2Si

    data class C(val v: Int) : Wl2Si
}
