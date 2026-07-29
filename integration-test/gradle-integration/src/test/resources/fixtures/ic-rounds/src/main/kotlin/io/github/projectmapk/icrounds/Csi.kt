package io.github.projectmapk.icrounds

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 多段中間チェーンの基底（CSI ← Mid1 ← Mid2 ← ChLeaf を各別ファイルに配置する）
@Enumize
sealed interface CSI
