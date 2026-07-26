package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-36: 末端の単純名が中間 sealed の名前と一致 → LABEL_CLASH 非発火
class NmMidOuter {
    data object Same : NmMid
}
