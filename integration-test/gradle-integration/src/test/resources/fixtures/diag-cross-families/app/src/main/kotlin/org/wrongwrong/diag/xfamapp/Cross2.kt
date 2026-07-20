package org.wrongwrong.diag.xfamapp

import org.wrongwrong.diag.xfam.XfamSi1
import org.wrongwrong.diag.xfam.XfamSi2

// TC-DIAG-094: 別モジュールで 2 家族の末端 interface を実装 → 言語エラー（doc 上は MULTIPLE_FAMILIES でも先回り報告）
class Cross2 : XfamSi1.LeafA, XfamSi2.LeafB
