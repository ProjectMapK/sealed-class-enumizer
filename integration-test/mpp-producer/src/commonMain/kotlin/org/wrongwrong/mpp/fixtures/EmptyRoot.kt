package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// 空階層（末端ゼロ）の MPP / 跨モジュール観測フィクスチャ（docs/テストケース管理.md TC-GAP-018）。
// entries = [] で診断は非発火、valueOf は常に IllegalArgumentException になる
@Enumize sealed interface EmptyRoot
