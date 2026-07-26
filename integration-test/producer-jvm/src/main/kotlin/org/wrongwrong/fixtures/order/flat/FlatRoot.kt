package org.wrongwrong.fixtures.order.flat

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間無し 17 末端の順序フィクスチャ基底（docs/test/ケース03-順序.md §2.1・ORD-01/ORD-02/ORD-05/ORD-06）。
// 本ファイルは「同一ファイルトップレベル」配置軸（K7）と宣言順≠FQN 順の要請（ORD-02）で複数トップレベル
// 宣言を同居させるため、1 ファイル 1 クラス規約の適用外とする。
//
// 期待 entries（label 列。FQN UTF-16 序数・ケース03 §2.1 の導出どおり）:
// [A1, AA, AB1, A_, Ab2, Az, Bbb, Aaa, Foo, FooBar, Mmm, Q, Sep0, Zzz, aB3, aLower, ab4]
@Enumize
sealed interface FlatRoot {
    // 基底ネスト配置の末端（ClassId 残部 = FlatRoot.Aaa）
    data object Aaa : FlatRoot
}

// 同一ファイルトップレベル配置の文字境界群。宣言順は FQN 順（A1 → AA → A_ → Az）と意図的に逆へ配置し、
// entries が宣言順に依存しないことの入力とする（ORD-02）

data object Az : FlatRoot

data object A_ : FlatRoot

data object AA : FlatRoot

data object A1 : FlatRoot
