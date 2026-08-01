package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-53: supertype 直接型引数の射影・nullable は言語が先回りし MSM 不到達

// 射影 → 言語 PROJECTION_IN_IMMEDIATE_ARGUMENT_TO_SUPERTYPE
@Enumize
sealed interface Proj : Enumized<out Proj.Enumish> {
    data object L : Proj
}

// nullable 型引数は Enumized<out T : Enumish> の境界違反
@Enumize
sealed interface NulArg : Enumized<NulArg.Enumish?> {
    data object L : NulArg
}
