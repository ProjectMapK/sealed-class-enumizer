package io.github.projectmapk.mpp.fixtures.order

// ネスト末端 S.Aaa の外側クラス（docs/概要.md §5 の実測形）。単純名順なら Aaa が先頭に
// なるところ、FQN "S.Aaa" として並ぶため entries では 3 番目になる
class S {
    data object Aaa : FqnOrder
}
