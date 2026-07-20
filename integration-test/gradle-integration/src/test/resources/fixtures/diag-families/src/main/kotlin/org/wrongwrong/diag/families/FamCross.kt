package org.wrongwrong.diag.families

// TC-DIAG-013: 兄弟階層の交差（object X : SI1, SI2） → ENUMIZE_MULTIPLE_FAMILIES
data object FamCross : FamA, FamB
