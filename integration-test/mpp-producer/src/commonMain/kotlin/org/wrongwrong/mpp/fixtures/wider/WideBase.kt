package org.wrongwrong.mpp.fixtures.wider

import org.wrongwrong.sealedClassEnumizer.Enumize

// internal 基底（E-2 の生成側 MPP 版: 基底より広い可視性の末端は Wide.kt。エッジ §1.2・
// docs/テストケース管理.md TC-GAP-017）。階層 API（entries / valueOf）は internal、
// 値・kind API（asEnumish / label）は public という分離が全ターゲットで成立することを観測する
@Enumize
internal sealed interface WideBase
