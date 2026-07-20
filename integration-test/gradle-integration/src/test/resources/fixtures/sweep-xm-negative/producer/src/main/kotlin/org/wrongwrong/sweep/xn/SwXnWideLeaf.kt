package org.wrongwrong.sweep.xn

// TC-VIS-038 用: 基底より広い public 末端（public companion → 規則 1 で asEnumish は具体型を返す）
class SwXnWideLeaf(val v: Int) : SwXnWideBase {
    companion object
}
