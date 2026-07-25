package org.wrongwrong.sweep.fail

// TC-LEAF-090: 別ファイルの private 外側クラスにネストした末端 → 基底本体スコープから名指し不可だが、
// 末端ファイルのトップレベル IR-only アクセサ経由で entries に載る
// （docs/コンパイラプラグイン設計02.md §4.3・docs/エッジケースへの対応方針.md §1.4。診断は出ない）
private class SwKnaOuter {
    object Leaf : SwKnaSi
}
