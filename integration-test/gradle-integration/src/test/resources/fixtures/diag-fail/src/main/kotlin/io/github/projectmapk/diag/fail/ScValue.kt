package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-64: value class は class を継承できない → 言語エラー
@JvmInline
value class ScValue(val v: Int) : Sc()
