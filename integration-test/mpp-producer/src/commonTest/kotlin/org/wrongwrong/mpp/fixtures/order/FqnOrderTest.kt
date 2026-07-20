package org.wrongwrong.mpp.fixtures.order

import kotlin.test.Test
import kotlin.test.assertEquals

// entries 順序が全ターゲットで FQN 序数順に一致することの box テスト
// （docs/テストケース管理.md TC-MPP-006・docs/概要.md §5）
class FqnOrderTest {
    // 単純名順（Aaa 先頭）・宣言順（Zzz 先頭）・大小無視（aLower 先頭）のいずれでもない並びが
    // 全ターゲットで一致する
    @Test
    fun entriesFollowFqnOrdinalOrderOnEveryTarget() {
        assertEquals(
            listOf("Bbb", "Mmm", "Aaa", "Zzz", "aLower"),
            FqnOrder.Enumish.entries.map { it.label },
        )
    }
}
