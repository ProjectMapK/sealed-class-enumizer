package org.wrongwrong.diag.label

// TC-DIAG-098: enum 末端の単純名と衝突する別末端 → ENUMIZE_LABEL_CLASH
class Lc3Outer {
    object Dup : Lc3Si
}
