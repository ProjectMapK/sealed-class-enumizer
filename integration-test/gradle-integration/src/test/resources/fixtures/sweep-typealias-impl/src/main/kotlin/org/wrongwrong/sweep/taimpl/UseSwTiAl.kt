package org.wrongwrong.sweep.taimpl

// UseSwTiEx と同一形。別名形の手動実装が継承者一覧に載らなければ、この when は網羅と見なされる
fun useSwTiAl(kind: SwTiAl.Enumish): Int = when (kind) {
    SwTiAlLeaf.Companion -> 1
}
