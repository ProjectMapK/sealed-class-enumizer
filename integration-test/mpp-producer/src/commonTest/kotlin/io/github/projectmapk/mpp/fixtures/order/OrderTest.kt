package io.github.projectmapk.mpp.fixtures.order

import kotlin.test.Test
import kotlin.test.assertEquals

// entries 順序（FQN 序数順・DFS break 順）が全ターゲットで一致することの box テスト
// （docs/test/ケース05-境界横断.md XMP-36・docs/概要.md §5）。commonTest は各 platform で
// 実行されるため、1 本の期待値との一致が metadata / platform の決定的一致（V5×順序）の実測になる
class OrderTest {
    // 単純名順（Aaa 先頭）・宣言順（Zzz 先頭）・大小無視（aLower 先頭）のいずれでもない並びが
    // 全ターゲットで一致する
    @Test
    fun entriesFollowFqnOrdinalOrder() {
        assertEquals(
            listOf("Bbb", "Mmm", "Aaa", "Zzz", "aLower"),
            FqnOrder.Enumish.entries.map { it.label },
        )
    }

    // BreakRoot 継承者 [Bbb, BreakMid]（FQN 順）→ BreakMid を [Aaa] へ展開 → entries=[Bbb, Aaa]。
    // 末端集合の FQN 順 [Aaa, Bbb] にはならない（docs/コンパイラプラグイン設計00.md §6.2 の break が全ターゲットで同一）
    @Test
    fun breakOrderIsIdentical() {
        assertEquals(listOf("Bbb", "Aaa"), BreakRoot.Enumish.entries.map { it.label })
    }
}
