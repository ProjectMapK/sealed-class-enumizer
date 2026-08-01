package io.github.projectmapk.mpp.fixtures.kindaccessor

// 基底のファイル外配置が要請となる KaRoot 末端群（docs/コンパイラプラグイン設計02.md §4.3）。
// private は宣言ファイル内でのみ可視であり、本ファイルに置くこと自体がトップレベル壁の成立条件である。
// 生成されるトップレベルアクセサは kind の外側クラス連鎖を名前へ含むため、1 ファイルに複数の壁が
// 同居しても衝突しない

// 別ファイルの private トップレベル末端 → 本ファイルのトップレベル IR-only アクセサ経由で load
private object KaPrivTop : KaRoot

// private 外側クラスにネストした参照不能末端 → 本ファイルのトップレベル IR-only アクセサ経由で load
private class KaHiddenHost {
    object Leaf : KaRoot
}
