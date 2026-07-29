package io.github.projectmapk.mpp.fixtures

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 空階層（末端ゼロ）の MPP / 跨モジュール観測フィクスチャ（docs/test/ケース05-境界横断.md XMP-08）。
// entries = [] で診断は非発火、valueOf は常に IllegalArgumentException になる
@Enumize sealed interface EmptyRoot
