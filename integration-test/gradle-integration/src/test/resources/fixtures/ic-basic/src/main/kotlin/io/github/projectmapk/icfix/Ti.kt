package io.github.projectmapk.icfix

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 第 1 階層と独立した第 2 階層（P3 非集約 = docs/test/ケース06-ビルド動態.md BLD-22 の非 dirty 観測用）
@Enumize
sealed interface TI {
    data object T1 : TI
}
