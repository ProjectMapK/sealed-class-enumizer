package org.wrongwrong.fixtures.companionleaf

// 名前つき companion が単独で末端（TC-BOX-023）。companion 自身が末端の場合に限り label = 宣言名 "Named"
class WithNamed {
    companion object Named : Token
}
