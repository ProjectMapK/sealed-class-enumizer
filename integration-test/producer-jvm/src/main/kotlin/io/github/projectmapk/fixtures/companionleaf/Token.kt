package io.github.projectmapk.fixtures.companionleaf

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 階層外クラスの companion が単独で末端になる許容構成（docs/test/ケース01-生成と実行時API.md
// API-25/API-26・docs/test/ケース03-順序.md ORD-08）。
// 基底 / 中間の companion 末端の成立形は sealedbase / mid が担う。
//
// 期待 entries（label 列。FQN UTF-16 序数）: [Zzz, Companion, HostA, Named]
@Enumize sealed interface Token

// Host 自身は階層外で、companion のみが末端（API-25。kind = companion 自身・label = 宣言名 "Companion"）。
// ENUMIZE_COMPANION_LEAF_CONFLICT は外側 = 末端の場合にのみ発火し、この構成は許容される
class Host {
    companion object : Token
}

// 名前つき companion が単独で末端（API-26。companion 自身が末端の場合に限り label = 宣言名 "Named"）
class WithNamed {
    companion object Named : Token
}

// 序数境界の対照末端（ORD-08）: p.Host.Companion と p.HostA は
// 共通接頭辞 "Host" の後 '.'(46) < 'A'(65) のため Host.Companion が HostA より先行する
data object HostA : Token

// 名前つき companion（Zzz）が末端となる順序プローブ（ORD-08）。
// companion 自身が末端の場合は宣言名が label と末端 ClassId（= 順序キー）の両方に効き、
// p.Holder2.Zzz は共通接頭辞 "Ho" の後 'l'(108) < 's'(115) の比較で p.Host.Companion より先行する
class Holder2 {
    companion object Zzz : Token
}
