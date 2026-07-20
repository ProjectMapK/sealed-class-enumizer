package org.wrongwrong.diag.nmvis

// TC-DIAG-090: 別ファイルの private トップレベル中間 sealed → 非発火（中間は kind でなく名前参照されない）
private sealed interface NmVis3Mid : NmVis3 {
    data object MLeaf : NmVis3Mid
}
