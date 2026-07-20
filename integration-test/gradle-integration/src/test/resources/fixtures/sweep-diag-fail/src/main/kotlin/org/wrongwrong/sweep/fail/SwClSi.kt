package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-LEAF-067 / TC-MAN-032: 末端 class の既存 companion（= kind・生成先）が label を手動宣言
// → ENUMIZE_MANUAL_MEMBER_CONFLICT（末端本体の label は警告どまり = TC-DIAG-063 との対比）
@Enumize
sealed interface SwClSi {
    class Foo(val v: Int) : SwClSi {
        companion object {
            override val label: String get() = "manual"
        }
    }
}
