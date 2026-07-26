package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-35: companion 末端の宣言名と衝突する別末端 → LABEL_CLASH
class Lc2Outer {
    object Foo2 : Lc2Si
}
