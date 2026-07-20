package org.wrongwrong.diag.mmc

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-044: kind（末端 object）の label 手動宣言 → ENUMIZE_MANUAL_MEMBER_CONFLICT
@Enumize
sealed interface Mm1Si {
    data object Bad : Mm1Si {
        override val label: String get() = "manual"
    }
}
