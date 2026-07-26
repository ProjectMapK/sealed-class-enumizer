package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-53: supertype 直接型引数の射影は言語が先回りし MSM 不到達
@Enumize
sealed interface Proj : Enumized<out Proj.Enumish> {
    data object L : Proj
}
