package org.wrongwrong.sweep.fail

// TC-LEAF-090: 別ファイルの private 外側クラスにネストした末端 → 基底本体スコープから参照不能で
// ENUMIZE_KIND_NOT_ACCESSIBLE（明示 2 トリガ以外の「private 外側」経由も同一規則で捕捉される）
private class SwKnaOuter {
    object Leaf : SwKnaSi
}
