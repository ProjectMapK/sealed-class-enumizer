package org.wrongwrong.fixtures.manualimpl

import org.wrongwrong.sealedClassEnumizer.Enumize

// 手動実装の許容（docs/概要.md §8）: 階層内による生成 Enumish の実装（末端 class 自身による
// 実装 = ManualLeaf.kt）はエラーにならず、その値は kind ではないため entries / valueOf に現れない。
// 階層外からの直接実装は継承者一覧へ反映する経路が無いため
// ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY のコンパイルエラーになる
@Enumize
sealed interface WithManual {
    data object Real : WithManual
}
