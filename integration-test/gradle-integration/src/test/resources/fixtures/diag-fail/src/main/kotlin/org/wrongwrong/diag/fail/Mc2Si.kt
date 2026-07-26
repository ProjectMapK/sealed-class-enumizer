package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-40: 判定は callable 名単位（宣言種別交差・引数付き過負荷でも MC）
@Enumize
sealed interface Mc2Si {
    data object Fn : Mc2Si {
        fun label(): String = "fn"
    }

    data class Ctor(val asEnumish: Int) : Mc2Si {
        companion object
    }

    data object Over : Mc2Si {
        fun asEnumish(tag: String): String = tag
    }
}
