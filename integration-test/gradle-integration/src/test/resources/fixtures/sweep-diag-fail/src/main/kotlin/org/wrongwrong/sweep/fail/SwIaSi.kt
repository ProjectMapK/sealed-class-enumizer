package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-063: 末端 interface の asEnumish 手動宣言（生成先 = interface 自身の default 実装）
// → ENUMIZE_MANUAL_MEMBER_CONFLICT（TC-DIAG-045 = 末端 class 版の interface 対応物）
@Enumize
sealed interface SwIaSi {
    interface Custom : SwIaSi {
        override fun asEnumish(): SwIaSi.Enumish = Companion

        companion object
    }
}
