package org.wrongwrong.diag.warn

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-064: 基底自身が label という可視メンバーを宣言 → ENUMIZE_EXTENSION_SHADOWED（警告）
@Enumize
sealed interface Wl2Si {
    val label: String get() = "base"

    data object L : Wl2Si
}
