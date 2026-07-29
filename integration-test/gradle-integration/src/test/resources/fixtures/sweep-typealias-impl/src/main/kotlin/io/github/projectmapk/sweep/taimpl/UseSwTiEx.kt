package io.github.projectmapk.sweep.taimpl

// 手動実装の枝を欠いた kind-when。手動実装が継承者一覧に載っていれば網羅性エラーになる
fun useSwTiEx(kind: SwTiEx.Enumish): Int = when (kind) {
    SwTiExLeaf.Companion -> 1
}
