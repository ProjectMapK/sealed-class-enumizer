package org.wrongwrong.fixtures.manual.impl

import org.wrongwrong.sealedClassEnumizer.Enumize

// 手動実装の許容フィクスチャ基底（docs/test/ケース01-生成と実行時API.md API-40/API-41）。
// 階層内による生成 Enumish の実装はエラーにならず、class 形の手動実装値は kind ではないため
// entries / valueOf に現れない。階層外からの直接実装は ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY
// （ケース04 が正典）
@Enumize
sealed interface WithManual {
    data object Real : WithManual
}
