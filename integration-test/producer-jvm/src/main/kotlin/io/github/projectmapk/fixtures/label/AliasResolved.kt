package io.github.projectmapk.fixtures.label

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize

// 明示 label による単純名衝突の解消（docs/test/ケース01-生成と実行時API.md API-56）。
// 同一単純名 Same の末端を基底ネストと階層外クラスのネストへ 1 つずつ置き、後者を明示 label で解消する
@Enumize
sealed interface AliasResolved {
    data object Same : AliasResolved
}

// AliasResolved.Same と同一単純名の末端を持つ階層外クラス
class AliasNs {
    @EnumishLabel("SameInNs") data object Same : AliasResolved
}
