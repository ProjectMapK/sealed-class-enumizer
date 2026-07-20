package org.wrongwrong.diag.label

// TC-DIAG-040: companion-as-leaf の宣言名と衝突する別末端 → ENUMIZE_LABEL_CLASH
class Lc2Outer {
    object Foo2 : Lc2Si
}
