package io.github.projectmapk.probe.alias

// docs/test/ケース04-診断.md DIA-67 用: @Enumize 自体への typealias。
// AaTypealias.kt はテスト側が除去するため、別名宣言はそれと別ファイルに置く
typealias EnumizeAlias = io.github.projectmapk.sealedClassEnumizer.Enumize
