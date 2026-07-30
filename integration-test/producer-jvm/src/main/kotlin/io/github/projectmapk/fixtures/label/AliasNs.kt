package io.github.projectmapk.fixtures.label

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel

// AliasResolved.Same と同一単純名の末端を階層外クラスのネストに持ち、明示 label で
// LABEL_CLASH を解消する構成（docs/test/ケース01-生成と実行時API.md API-56）
class AliasNs {
    @EnumishLabel("SameInNs") data object Same : AliasResolved
}
