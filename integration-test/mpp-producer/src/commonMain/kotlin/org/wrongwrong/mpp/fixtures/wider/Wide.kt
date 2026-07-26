package org.wrongwrong.mpp.fixtures.wider

// 基底（internal）より広い可視性の末端（docs/エッジケースへの対応方針.md §1.1 #2）。companion は public（= 末端以上）の
// ため規則 1 が適用され、asEnumish の返り値型は Wide.Companion になる（規則 3 の
// ENUMIZE_KIND_TYPE_NOT_DENOTABLE は発火しない。docs/test/ケース05-境界横断.md XMP-43 の正値側）
class Wide : WideBase {
    companion object
}
