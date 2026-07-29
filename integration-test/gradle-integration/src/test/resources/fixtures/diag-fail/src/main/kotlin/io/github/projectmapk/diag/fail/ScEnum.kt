package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-64: enum class は class を継承できない → 言語エラー
enum class ScEnum : Sc() {
    ONE,
}
