package io.github.projectmapk.mpp.fixtures.order

// ネスト末端 Box.Bbb の外側クラス（docs/概要.md §5 の実測形）。FQN "Box.Bbb" が
// 序数最小のため entries では先頭になる
class Box {
    data object Bbb : FqnOrder
}
