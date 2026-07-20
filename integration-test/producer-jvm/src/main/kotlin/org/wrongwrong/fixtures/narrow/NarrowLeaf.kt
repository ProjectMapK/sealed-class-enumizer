package org.wrongwrong.fixtures.narrow

// internal 末端 + internal companion（TC-VIS-029 / TC-VIS-060）。
// eff(C) = eff(L) = eff(基底) = internal → 規則 2 は発火せず規則 1（具体型 NarrowLeaf.Companion）
internal class NarrowLeaf(val v: Int) : NarrowBase {
    internal companion object
}
