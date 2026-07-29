package io.github.projectmapk.mpp.fixtures.jvm

import io.github.projectmapk.sealedClassEnumizer.Enumize

// platform 専用ソースセット（jvmMain）に置く @Enumize 階層（docs/test/ケース05-境界横断.md XMP-41）。
// metadata コンパイルを経ない通常の platform 生成として成立し、V5 の成否に依存しない
@Enumize
sealed interface JvmOnly {
    data object A : JvmOnly

    data class B(val v: Int) : JvmOnly
}
