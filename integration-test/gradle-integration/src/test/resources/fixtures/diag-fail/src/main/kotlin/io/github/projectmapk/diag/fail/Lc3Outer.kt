package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-34: enum 末端の単純名と衝突する別末端 → LABEL_CLASH
class Lc3Outer {
    object Dup : Lc3Si
}
