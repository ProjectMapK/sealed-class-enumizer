package org.wrongwrong.det

// トップレベル中間 sealed（docs/コンパイラプラグイン設計00.md §6.2 の入れ子展開で全体が FQN 順から乖離する構成）。
// 内部に 2 末端を持ち、宣言順入れ替え編集（docs/test/ケース06-ビルド動態.md BLD-04）の対象になる
sealed interface Mid : S {
    data object MA : Mid

    data object MB : Mid
}
