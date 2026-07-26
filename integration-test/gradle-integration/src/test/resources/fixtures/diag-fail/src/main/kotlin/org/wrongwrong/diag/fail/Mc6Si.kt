package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-42: 末端 interface の asEnumish 手動宣言 → MMC
@Enumize
sealed interface IaSi {
    interface Custom : IaSi {
        override fun asEnumish(): IaSi.Enumish = Companion

        companion object
    }
}
