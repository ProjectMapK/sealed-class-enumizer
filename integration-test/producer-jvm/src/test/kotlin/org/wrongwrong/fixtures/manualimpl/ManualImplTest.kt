package org.wrongwrong.fixtures.manualimpl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// 手動実装の許容（docs/概要.md §8）: kind 以外の Enumish 実装は entries / valueOf に現れない
class ManualImplTest {
    @Test
    fun manualImplementationIsNotAnEntry() {
        assertEquals(listOf("Real"), WithManual.Enumish.entries.map { it.label })
    }

    @Test
    fun manualImplementationIsNotResolvedByValueOf() {
        assertNull(WithManual.Enumish.valueOfOrNull("Rogue"))
    }

    // 手動実装そのものは通常のオブジェクトとして機能する（enum 的 API の保証対象外なだけ）
    @Test
    fun manualImplementationStillWorksAsEnumish() {
        val rogue: WithManual.Enumish = RogueKind
        assertEquals("Rogue", rogue.label)
    }
}
