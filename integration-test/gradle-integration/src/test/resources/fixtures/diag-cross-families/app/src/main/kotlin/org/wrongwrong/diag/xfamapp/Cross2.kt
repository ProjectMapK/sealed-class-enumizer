package org.wrongwrong.diag.xfamapp

import org.wrongwrong.diag.xfam.XfamSi1
import org.wrongwrong.diag.xfam.XfamSi2

// TC-DIAG-094: 別モジュールで 2 階層の末端 interface を実装 → Enumized の型引数不一致の言語エラー
class Cross2 : XfamSi1.LeafA, XfamSi2.LeafB
