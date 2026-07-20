package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-LEAF-068: 末端自身が型引数の異なる Enumized を手動継承（基底側 = TC-DIAG-050 の末端側対応物）
// → ENUMIZE_MANUAL_SUPERTYPE_MISMATCH（同一総称 IF の型引数二重継承の先回り検出）
data class SwLeafMm(val v: Int) : SwTfA, Enumized<SwWrong> {
    companion object
}
