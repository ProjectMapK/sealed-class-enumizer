package io.github.projectmapk.fixtures.vis.narrow

// internal 末端 × internal companion（docs/test/ケース02-可視性.md VIS-10 = 規則 1 境界:
// eff(C) = eff(L) = internal の実効同値で規則 2 は発火せず具体型 NarrowLeaf.Companion のまま）
internal class NarrowLeaf(val v: Int) : NarrowBase {
    internal companion object
}
