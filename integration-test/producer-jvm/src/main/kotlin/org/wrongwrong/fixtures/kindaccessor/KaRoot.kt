package org.wrongwrong.fixtures.kindaccessor

import org.wrongwrong.sealedClassEnumizer.Enumize

// 参照不能 kind（private / protected companion・private トップレベル末端）が IR-only アクセサ経由で
// entries に載ることの実挙動フィクスチャ（概要 §8・設計02 §4.3）。旧 ENUMIZE_KIND_NOT_ACCESSIBLE の
// 各構成が、エラーではなく load として成立する
@Enumize
sealed interface KaRoot {
    // 基底内ネストの public 末端（参照可能 = 直接参照。アクセサ生成の対象外）
    data object Visible : KaRoot
}
