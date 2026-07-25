package org.wrongwrong.diag.warn

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-064: 基底自身が label という可視メンバーを宣言 → ENUMIZE_EXTENSION_SHADOWED（警告）
@Enumize
sealed interface Wl2Si {
    val label: String get() = "base"

    // TC-DIAG-115: kind（末端 object）は生成 label が継承を override するため非発火
    data object L : Wl2Si

    // TC-DIAG-114: 基底の label を継承した末端 class → 継承由来の警告（宣言元 = 基底）
    data class C(val v: Int) : Wl2Si
}
