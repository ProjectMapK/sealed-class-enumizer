package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-041: kind を担うだけの named companion の同名（Factory） → LABEL_CLASH 非発火（label は末端単純名）
@Enumize
sealed interface NmFac {
    class FooL(val v: Int) : NmFac {
        companion object Factory
    }

    class BarL(val v: Int) : NmFac {
        companion object Factory
    }
}
