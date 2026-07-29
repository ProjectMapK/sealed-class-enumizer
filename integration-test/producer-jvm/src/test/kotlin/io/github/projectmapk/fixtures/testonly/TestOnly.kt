package io.github.projectmapk.fixtures.testonly

import io.github.projectmapk.sealedClassEnumizer.Enumize

// test compilation 配線の検証フィクスチャ（docs/test/ケース05-境界横断.md XMP-25）。
// gradle-plugin は test compilation へ runtime-api を implementation 配線で供給する（KT-63142 警告回避）。
// src/test 内の @Enumize 階層と生成 API 利用がコンパイル成立すること自体が検証である
@Enumize
sealed interface TestOnly {
    data object On : TestOnly

    data class Off(val reason: String) : TestOnly
}

// 生成 API を test compilation から参照する利用点（この参照の成立が配線の証明）
fun testOnlyLabels(): List<String> = TestOnly.Enumish.entries.map { it.label }
