package io.github.projectmapk.fixtures.order.flat

// FlatRoot の別ファイル配置末端群（docs/test/ケース03-順序.md §2.1・ORD-01）。
// K7 の配置値のうち「別ファイルトップレベル」と「無関係クラスネスト」を担い、
// 基底と同一ファイルの配置値は FlatRoot.kt が担う。
// 序数境界の役割は各宣言のコメントとケース03 §2.1 の導出表が持つ

// --- 別ファイルトップレベル配置 × 文字境界 ---

data object AB1 : FlatRoot

data object Ab2 : FlatRoot

// 'Z'(90) < 'a'(97) の直後域（小文字始まり × 大文字 2 文字目）
data object aB3 : FlatRoot

// 全大文字名の後へ整列する小文字始まり
data object aLower : FlatRoot

// 'L'(76) < 'b'(98) で aLower の後（小文字一色）
data object ab4 : FlatRoot

data object Mmm : FlatRoot

// 大文字域の末尾側
data object Zzz : FlatRoot

// --- 接頭辞対（短い方が先） ---

// class 末端（自動生成 companion が kind）: kind ClassId は Foo.Companion となるが、
// '.'(46) < 'B'(66) により inheritors 順でも FooBar より先行が保たれる
class Foo : FlatRoot

data object FooBar : FlatRoot

// --- セパレータ対（ClassId 残部 Sep.Q の '.'(46) < Sep0 の '0'(48) でネスト側が先行する） ---

// 無関係クラス内ネスト配置
class Sep {
    data object Q : FlatRoot
}

data object Sep0 : FlatRoot

// --- 無関係クラス内ネスト配置の末端 Bbb（ClassId 残部 = Box.Bbb） ---

class Box {
    data object Bbb : FlatRoot
}
