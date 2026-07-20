package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-066: 末端 enum class の companion（= kind・label の生成先）の label 手動宣言
// → ENUMIZE_MANUAL_MEMBER_CONFLICT（enum の kind は通常の末端 class の companion と同一扱い）
@Enumize
sealed interface SwEcSi {
    enum class Builtin : SwEcSi {
        HELP,
        VERSION,
        ;

        companion object {
            override val label: String get() = "custom"
        }
    }
}
