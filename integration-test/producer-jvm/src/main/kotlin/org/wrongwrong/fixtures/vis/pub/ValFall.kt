package org.wrongwrong.fixtures.vis.pub

// public value class 末端 × internal companion（docs/test/ケース02-可視性.md VIS-14 = 規則 2 × 種別。
// boxing を跨いでも kind は同一のまま）
@JvmInline
value class ValFall(val x: Int) : VisRoot {
    internal companion object
}
