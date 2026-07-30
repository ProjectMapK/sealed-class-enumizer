package io.github.projectmapk.probe.alias

// docs/test/ケース04-診断.md DIA-67: typealias 表記は述語（エイリアス展開前）に載らず生成が
// 走らないため ENUMIZE_ALIASED_ANNOTATION（Main からは参照しない）
@EnumizeAlias
sealed interface AaTa {
    data object T1 : AaTa
}
