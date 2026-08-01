package io.github.projectmapk.diag.ok

// docs/test/ケース04-診断.md DIA-26: 別ファイルの private トップレベル中間 sealed → 非発火。
// private トップレベルはファイルスコープであり、基底（VisibilityNearMiss.kt の NmVis3）と
// 別ファイルであること自体が本ケースの成立条件である
private sealed interface NmVis3Mid : NmVis3 {
    data object MLeaf : NmVis3Mid
}
