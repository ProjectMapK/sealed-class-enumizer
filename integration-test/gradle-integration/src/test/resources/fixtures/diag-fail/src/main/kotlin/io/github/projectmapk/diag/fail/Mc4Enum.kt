package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-41: enum 末端の kind companion の label 手動宣言 → MC
@Enumize
sealed interface Mc4Enum {
    enum class Builtin : Mc4Enum {
        HELP,
        ;

        companion object {
            override val label: String get() = "custom"
        }
    }
}
