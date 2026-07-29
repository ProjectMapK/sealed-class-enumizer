package io.github.projectmapk.shared

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 同一ファイルに 2 つの独立した @Enumize 階層を同居させる境界フィクスチャ（docs/test/ケース06-ビルド動態.md BLD-32）。
// P3（非集約）は論理集約の話であり、同一ファイル内では再コンパイル単位がファイルのため物理的に
// 共連れになる。2 階層の同居がテストの本質のため、1 ファイル 1 クラスの規約はこのファイルに限り適用外
@Enumize
sealed interface SA {
    data object A1 : SA
}

@Enumize
sealed interface SB {
    data object B1 : SB
}
