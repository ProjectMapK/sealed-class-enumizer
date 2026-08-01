package io.github.projectmapk.mpp.fixtures.wider

import io.github.projectmapk.sealedClassEnumizer.Enumize

// internal 基底（E-2 の生成側 MPP 版。docs/エッジケースへの対応方針.md §1.1 #2・§1.2・
// docs/test/ケース05-境界横断.md XMP-43）。階層 API（entries / valueOf）は internal、
// 値・kind API（asEnumish / label）は public という分離が全ターゲットで成立することを観測する
@Enumize internal sealed interface WideBase

// 基底（internal）より広い可視性の末端。companion は public（= 末端以上）のため規則 1 が適用され、
// asEnumish の返り値型は Wide.Companion になる（規則 3 の ENUMIZE_KIND_TYPE_NOT_DENOTABLE は発火しない）
class Wide : WideBase {
    companion object
}
