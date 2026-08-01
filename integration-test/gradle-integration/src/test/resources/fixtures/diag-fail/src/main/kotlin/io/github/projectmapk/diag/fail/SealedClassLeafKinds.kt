package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-64: sealed class 基底の末端種別境界。
// class を継承できない種別は言語エラーへ委譲され、プラグイン診断は出さない

@Enumize
sealed class Sc {
    data object Ok : Sc()
}

// annotation class は supertype を持てない
annotation class ScAnn : Sc()

// enum class は class を継承できない
enum class ScEnum : Sc() {
    ONE,
}

// interface は class を継承できない
interface ScIface : Sc

// value class は class を継承できない
@JvmInline
value class ScValue(val v: Int) : Sc()
