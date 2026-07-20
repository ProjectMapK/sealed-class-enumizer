package org.wrongwrong.diag.xweapp

import org.wrongwrong.diag.xwe.WeSi

// TC-DIAG-078: 生成 Enumish 上の kind 単位の網羅 when（else なし・V1-a）。末端追加で非網羅エラーになる
fun describe(e: WeSi.Enumish): Int = when (e) {
    WeSi.A -> 1
    WeSi.B -> 2
}
