package org.wrongwrong.diag.label

// TC-DIAG-039: 同一パッケージの別の外側にネストした同名末端 → ENUMIZE_LABEL_CLASH（両末端に報告）
class LcOuter1 {
    object Foo : LcSi
}
