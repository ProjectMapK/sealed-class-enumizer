package org.wrongwrong.fixtures.companionleaf

// Host 自身は階層外で、companion のみが末端（docs/test/ケース01-生成と実行時API.md API-25。
// kind = companion 自身・label = 宣言名 "Companion"）。
// ENUMIZE_COMPANION_LEAF_CONFLICT は外側 = 末端の場合にのみ発火し、この構成は許容される
class Host {
    companion object : Token
}
