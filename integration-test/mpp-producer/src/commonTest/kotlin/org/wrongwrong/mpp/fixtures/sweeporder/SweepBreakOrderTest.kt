package org.wrongwrong.mpp.fixtures.sweeporder

import kotlin.test.Test
import kotlin.test.assertEquals

// break 階層の DFS 展開順の全ターゲット一致（docs/テストケース管理.md TC-ORD-063）。
// commonTest は各 platform で実行されるため、1 本の期待値との一致が metadata / platform の
// 決定的一致（V5×順序）の実測になる
class SweepBreakOrderTest {
    // BreakRoot 継承者 [Bbb, BreakMid]（FQN 順）→ BreakMid を [Aaa] へ展開 → entries=[Bbb, Aaa]。
    // 末端集合の FQN 順 [Aaa, Bbb] にはならない（設計00 §6.2 の break が全ターゲットで同一）
    @Test
    fun breakOrderIsIdenticalOnEveryTarget() {
        assertEquals(listOf("Bbb", "Aaa"), BreakRoot.Enumish.entries.map { it.label })
    }
}
