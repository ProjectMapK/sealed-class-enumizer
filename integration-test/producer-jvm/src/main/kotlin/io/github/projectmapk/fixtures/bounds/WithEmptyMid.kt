package io.github.projectmapk.fixtures.bounds

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 継承者ゼロの中間 sealed（docs/test/ケース01-生成と実行時API.md API-49）:
// 中間 None は entries に寄与せず、空展開でも走査は縮退しない
@Enumize
sealed interface WithEmptyMid {
    sealed interface None : WithEmptyMid

    data object A : WithEmptyMid
}
