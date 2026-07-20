package org.wrongwrong.sweep.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-037: kind（末端 object）による enumishCompanion の override は許容（返り値型が
// object 型まで絞り込まれており同一オブジェクト以外を返せないため整合が壊れない）
@Enumize
sealed interface SwOkC {
    data object Bar : SwOkC {
        override val enumishCompanion: SwOkC.Enumish.Companion get() = SwOkC.Enumish
    }
}
