package org.wrongwrong.fixtures.companionleaf

// 順序の対照末端（TC-ORD-058）。p.Holder2.Zzz と p.Holder2A の比較は共通接頭辞 "Holder2" の後
// '.'(46) < 'A'(65) で決まる（宣言名 Zzz の 'Z' は比較位置に現れない）
data object Holder2A : Badge
