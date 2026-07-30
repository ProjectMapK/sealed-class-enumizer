package io.github.projectmapk.fixtures.label

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 明示 label による単純名衝突の解消の片側（docs/test/ケース01-生成と実行時API.md API-56）。
// もう片側（同一単純名 Same の別配置末端）は AliasNs.kt
@Enumize
sealed interface AliasResolved {
    data object Same : AliasResolved
}
