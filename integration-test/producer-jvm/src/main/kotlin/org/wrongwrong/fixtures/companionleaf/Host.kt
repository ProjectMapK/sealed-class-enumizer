package org.wrongwrong.fixtures.companionleaf

// Host 自身は階層外で、companion のみが末端（kind = companion 自身・label = 宣言名 "Companion"）。
// ENUMIZE_COMPANION_LEAF_CONFLICT は階層内の末端 class の companion にのみ発火し、この構成は許容される
class Host {
    companion object : Token
}
