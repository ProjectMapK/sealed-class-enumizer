package io.github.projectmapk.mpp.fixtures.order

import io.github.projectmapk.sealedClassEnumizer.Enumize

// entries 順序（FQN 序数順）の MPP 版フィクスチャ（docs/概要.md §5 の実測形・
// docs/test/ケース05-境界横断.md XMP-36）。ネスト末端 S.Aaa / Box.Bbb とトップレベル末端
// Mmm / Zzz / aLower を混在させ、entries = [Bbb, Mmm, Aaa, Zzz, aLower]（FQN 序数順）が
// 全ターゲットで一致することを観測する
@Enumize sealed interface FqnOrder

// ネスト末端 Box.Bbb の外側クラス。FQN "Box.Bbb" が序数最小のため entries では先頭になる
class Box {
    data object Bbb : FqnOrder
}

// トップレベル末端
data object Mmm : FqnOrder

// ネスト末端 S.Aaa の外側クラス。単純名順なら Aaa が先頭になるところ、
// FQN "S.Aaa" として並ぶため entries では 3 番目になる
class S {
    data object Aaa : FqnOrder
}

// トップレベル末端。'Z'(90) は 'S'(83) の後・'a'(97) の前のため entries では 4 番目になる
data object Zzz : FqnOrder

// 小文字始まりのトップレベル末端。大文字小文字を区別する序数比較では
// 'a'(97) > 'Z'(90) のため末尾になる（大小無視の辞書順なら先頭になる）
@Suppress("ClassName") data object aLower : FqnOrder
