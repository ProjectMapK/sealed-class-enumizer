package io.github.projectmapk.fixtures.companionleaf

// 名前つき companion（Zzz）が末端となる順序プローブ（docs/test/ケース03-順序.md ORD-08）。
// companion 自身が末端の場合は宣言名が label と末端 ClassId（= 順序キー）の両方に効き、
// p.Holder2.Zzz は共通接頭辞 "Ho" の後 'l'(108) < 's'(115) の比較で p.Host.Companion より先行する
class Holder2 {
    companion object Zzz : Token
}
