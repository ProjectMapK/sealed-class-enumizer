package org.wrongwrong.diag.nm

// TC-DIAG-099: 末端の単純名が中間 sealed の名前と一致 → LABEL_CLASH 非発火（判定は kind 同士のみ）
class NmMidOuter {
    data object Same : NmMid
}
