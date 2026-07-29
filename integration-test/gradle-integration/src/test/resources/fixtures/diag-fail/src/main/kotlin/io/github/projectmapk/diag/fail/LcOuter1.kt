package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-34: 同一単純名の末端対（当事者 1）→ ENUMIZE_LABEL_CLASH + 相手 FQN
class LcOuter1 {
    object Foo : LcSi
}
