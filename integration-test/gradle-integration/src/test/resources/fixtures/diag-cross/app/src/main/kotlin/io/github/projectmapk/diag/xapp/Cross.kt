package io.github.projectmapk.diag.xapp

import io.github.projectmapk.diag.xlib.XambSi

// docs/test/ケース04-診断.md DIA-18: 別 module で複数の末端 interface を実装（適用側）
// → ENUMIZE_AMBIGUOUS_KIND（利用側 module の宣言へ報告）
class Cross : XambSi.LeafA, XambSi.LeafB
