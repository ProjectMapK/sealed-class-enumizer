package org.wrongwrong.diag.xapp

import org.wrongwrong.diag.xlib.XfamSi1
import org.wrongwrong.diag.xlib.XfamSi2

// docs/test/ケース04-診断.md DIA-22: 2 階層の末端 interface 実装 → Enumized の型引数不一致の
// 言語エラーのみ（MULTIPLE_FAMILIES / AMBIGUOUS_KIND は不在）
class Cross2 : XfamSi1.LeafC, XfamSi2.LeafD
