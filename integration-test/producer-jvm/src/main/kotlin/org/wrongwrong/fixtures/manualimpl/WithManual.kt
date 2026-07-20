package org.wrongwrong.fixtures.manualimpl

import org.wrongwrong.sealedClassEnumizer.Enumize

// 手動実装の許容（docs/概要.md §8）: kind 以外による生成 Enumish の実装はエラーにならず、
// その値は kind ではないため entries / valueOf に現れない（手動実装は RogueKind.kt）
@Enumize
sealed interface WithManual {
    data object Real : WithManual
}
