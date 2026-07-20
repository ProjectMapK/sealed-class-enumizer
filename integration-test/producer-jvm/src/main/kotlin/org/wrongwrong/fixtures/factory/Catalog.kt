package org.wrongwrong.fixtures.factory

import org.wrongwrong.sealedClassEnumizer.Enumize

// 名前つき companion のフィクスチャ（TC-LEAF-021 / TC-VIS-025 / TC-MAN-044 near-miss）。
// label は末端宣言の単純名で不変（companion の宣言名 Factory に依存しない）
@Enumize
sealed interface Catalog {
    // public な名前つき companion（TC-LEAF-021）: kind = Made.Factory・規則 1（具体型）
    class Made(val v: Int) : Catalog {
        companion object Factory
    }

    // internal な名前つき companion（TC-VIS-025）: 規則 2 フォールバック + label は "Forged"
    class Forged(val v: Int) : Catalog {
        internal companion object Factory
    }
}
