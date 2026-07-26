package org.wrongwrong.fixtures.companionleaf

// 名前つき companion が単独で末端（docs/test/ケース01-生成と実行時API.md API-26）。
// companion 自身が末端の場合に限り label = 宣言名 "Named"
class WithNamed {
    companion object Named : Token
}
