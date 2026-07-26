package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-41: enum 末端の kind companion の label 手動宣言 → MMC
@Enumize
sealed interface McEnum {
    enum class Builtin : McEnum {
        HELP,
        ;

        companion object {
            override val label: String get() = "custom"
        }
    }
}
