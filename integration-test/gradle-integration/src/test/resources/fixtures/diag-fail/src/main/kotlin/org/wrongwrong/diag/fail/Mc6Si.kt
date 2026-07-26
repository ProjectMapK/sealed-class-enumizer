package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-42: 末端 interface の asEnumish 手動宣言 → MC
@Enumize
sealed interface Mc6Si {
    interface Custom : Mc6Si {
        override fun asEnumish(): Mc6Si.Enumish = Companion

        companion object
    }
}
