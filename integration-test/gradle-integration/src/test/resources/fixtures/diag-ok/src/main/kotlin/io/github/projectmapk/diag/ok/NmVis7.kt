package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: public 基底 + internal companion → 規則 2 フォールバックで非発火
@Enumize
sealed interface NmVis7 {
    class Half(val v: Int) : NmVis7 {
        internal companion object
    }
}
