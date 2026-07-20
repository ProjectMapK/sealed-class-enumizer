package org.wrongwrong.fixtures.samelabel

import org.wrongwrong.sealedClassEnumizer.Enumize

// FirstNs と同一パッケージ・同一単純名の末端を持つ別階層（TC-BOX-074）
@Enumize
sealed interface SecondNs {
    data object Same : SecondNs
}
