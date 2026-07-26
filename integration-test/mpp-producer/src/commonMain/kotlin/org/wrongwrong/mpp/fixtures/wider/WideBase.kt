package org.wrongwrong.mpp.fixtures.wider

import org.wrongwrong.sealedClassEnumizer.Enumize

// internal 基底（E-2 の生成側 MPP 版: 基底より広い可視性の末端は Wide.kt。docs/エッジケースへの対応方針.md §1.2・
// docs/test/ケース05-境界横断.md XMP-43）。階層 API（entries / valueOf）は internal、
// 値・kind API（asEnumish / label）は public という分離が全ターゲットで成立することを観測する
@Enumize internal sealed interface WideBase
