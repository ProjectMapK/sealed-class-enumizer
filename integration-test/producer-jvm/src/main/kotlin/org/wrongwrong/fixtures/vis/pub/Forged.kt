package org.wrongwrong.fixtures.vis.pub

// public 末端 × internal 名前つき companion（docs/test/ケース02-可視性.md VIS-13 = 規則 2。
// label は companion 名でなく末端単純名 "Forged" のまま不変）
class Forged(val v: Int) : VisRoot {
    internal companion object Factory
}
