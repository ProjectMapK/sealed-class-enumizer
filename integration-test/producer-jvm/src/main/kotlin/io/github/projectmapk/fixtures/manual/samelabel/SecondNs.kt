package io.github.projectmapk.fixtures.manual.samelabel

import io.github.projectmapk.sealedClassEnumizer.Enumize

// FirstNs と同一パッケージ・同一単純名の末端 Same を基底ネストに持つ独立階層
// （docs/test/ケース01-生成と実行時API.md API-43）
@Enumize
sealed interface SecondNs {
    data object Same : SecondNs
}
