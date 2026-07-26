package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-45: kind companion の手動 asEnumish（検査名集合外）→ 非発火
@Enumize
sealed interface OkKca {
    class Leaf(val v: Int) : OkKca {
        companion object {
            fun asEnumish(tag: String): String = tag
        }
    }
}
