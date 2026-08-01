package io.github.projectmapk.sweep.xn

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース05-境界横断.md XMP-17 用: internal 基底（基底より広い末端を持つ形。
// docs/エッジケースへの対応方針.md §1.1 #2 で言語上成立）
@Enumize
internal sealed interface SwXnWideBase

// 基底より広い public 末端（public companion → 規則 1 で asEnumish は具体型を返す）
class SwXnWideLeaf(val v: Int) : SwXnWideBase {
    companion object
}
