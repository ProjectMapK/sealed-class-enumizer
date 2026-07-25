package org.wrongwrong.sweep.tahead

import org.wrongwrong.sealedClassEnumizer.Enumize

// 手動 Enumized を typealias 経由で宣言した基底（展開後は Enumized<SwThSi.Enumish> と厳密一致）。
// 注入スキップの判定が展開後で行われることの観測点
@Enumize
sealed interface SwThSi : SwThAlias {
    data object L : SwThSi
}
