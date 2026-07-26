package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-45: kind の enumishCompanion override は許容
@Enumize
sealed interface OkC {
    data object Bar : OkC {
        override val enumishCompanion: OkC.Enumish.Companion get() = OkC.Enumish
    }
}
