package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.sealedClassEnumizer.Enumize

// platform 専用ソースセット（jvmMain）に置く @Enumize 階層（docs/テストケース管理.md TC-MPP-062）。
// metadata コンパイルを経ない通常の platform 生成として成立し、V5 の成否に依存しない
@Enumize
sealed interface JvmOnly {
    data object A : JvmOnly

    data class B(val v: Int) : JvmOnly
}
