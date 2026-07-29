package io.github.projectmapk.probe.alias

// docs/test/ケース04-診断.md DIA-67: typealias 表記の観測点。候補判定が未解決のアノテーション
// 型参照に触れて ICE となる（docs/test/保留.md GATE-02。Main からは参照しない）
@EnumizeAlias
sealed interface AaTa {
    data object T1 : AaTa
}
