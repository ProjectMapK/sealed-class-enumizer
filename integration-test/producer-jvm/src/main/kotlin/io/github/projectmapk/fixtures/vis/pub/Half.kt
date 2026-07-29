package io.github.projectmapk.fixtures.vis.pub

// public 末端 × internal 既定名 companion（docs/test/ケース02-可視性.md VIS-12 = 規則 2:
// eff(C)=internal < eff(L)=public で asEnumish の返り値型は VisRoot.Enumish へフォールバック）
class Half(val v: Int) : VisRoot {
    internal companion object
}
