package io.github.projectmapk.fixtures.bounds

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 継承者ゼロの空階層（docs/test/ケース01-生成と実行時API.md API-48）。診断は発火しない
@Enumize sealed interface Empty
