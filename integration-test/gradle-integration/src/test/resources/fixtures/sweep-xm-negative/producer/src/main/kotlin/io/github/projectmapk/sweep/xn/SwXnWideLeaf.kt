package io.github.projectmapk.sweep.xn

// docs/test/ケース05-境界横断.md XMP-17 用: 基底より広い public 末端（public companion → 規則 1 で asEnumish は具体型を返す）
class SwXnWideLeaf(val v: Int) : SwXnWideBase {
    companion object
}
