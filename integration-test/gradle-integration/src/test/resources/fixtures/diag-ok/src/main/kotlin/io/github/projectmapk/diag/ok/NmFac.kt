package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-36: kind を担うだけの named companion の同名（Factory）→ LABEL_CLASH 非発火
@Enumize
sealed interface NmFac {
    class FooL(val v: Int) : NmFac {
        companion object Factory
    }

    class BarL(val v: Int) : NmFac {
        companion object Factory
    }
}
