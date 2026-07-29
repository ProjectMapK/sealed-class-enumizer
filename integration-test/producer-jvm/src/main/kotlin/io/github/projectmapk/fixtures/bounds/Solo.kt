package io.github.projectmapk.fixtures.bounds

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 単一末端階層（docs/test/ケース01-生成と実行時API.md API-49。走査の下限境界）
@Enumize
sealed interface Solo {
    data object Only : Solo
}
