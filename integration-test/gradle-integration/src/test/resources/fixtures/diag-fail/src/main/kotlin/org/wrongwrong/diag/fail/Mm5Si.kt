package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-40: 判定は callable 名単位（宣言種別交差・引数付き過負荷でも MMC）
@Enumize
sealed interface Mm5Si {
    data object Fn : Mm5Si {
        fun label(): String = "fn"
    }

    data class Ctor(val asEnumish: Int) : Mm5Si {
        companion object
    }

    data object Over : Mm5Si {
        fun asEnumish(tag: String): String = tag
    }
}
