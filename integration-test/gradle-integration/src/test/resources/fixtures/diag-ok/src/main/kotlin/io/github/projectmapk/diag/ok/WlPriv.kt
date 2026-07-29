package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-39: private の label 宣言（可視条件の偽側）→ 非発火
@Enumize
sealed interface WlPriv {
    class Q : WlPriv {
        private val label: String = "q"

        companion object
    }
}
