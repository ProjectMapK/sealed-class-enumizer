package org.wrongwrong.sweep.ta

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-MAN-069: typealias 経由の手動 Enumized<SwTaAlias>（展開後は Enumized<SwTaSi.Enumish> と厳密一致）
// → 注入スキップで受容され MANUAL_SUPERTYPE_MISMATCH は非発火、が期待（D9X-14。実挙動の観測点）
@Enumize
sealed interface SwTaSi : Enumized<SwTaAlias> {
    data object L : SwTaSi
}
