package org.wrongwrong.fixtures.internalleaves

// internal class 末端 + 明示 internal companion（TC-VIS-023）。
// eff(C) = internal = eff(L) → 規則 1（asEnumish の返り値型は具体型 ClsLeaf.Companion）
internal class ClsLeaf(val v: Int) : InternalLeaves {
    internal companion object
}
