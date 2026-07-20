package org.wrongwrong.fixtures.emptyhier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

// 空階層と単一末端階層の境界 box テスト
// （docs/テストケース管理.md TC-LEAF-099 / TC-ORD-061 / TC-BOX-066 / TC-ORD-047 / TC-BOX-009）
class EmptyHierarchyTest {
    // TC-LEAF-099 / TC-ORD-061 / TC-BOX-066: entries は空・valueOf は常に例外・valueOfOrNull は常に null
    @Test
    fun emptyHierarchyHasEmptyEntries() {
        assertEquals(emptyList(), Empty.Enumish.entries)
        assertNull(Empty.Enumish.valueOfOrNull("X"))
        val failure = assertFailsWith<IllegalArgumentException> { Empty.Enumish.valueOf("X") }
        assertEquals("No enumish entry with label 'X' in Empty", failure.message)
    }

    // TC-BOX-066: 空でも lazy は一度だけ構築し同一の List 参照を返す
    @Test
    fun emptyEntriesAreStillMemoized() {
        assertSame(Empty.Enumish.entries, Empty.Enumish.entries)
    }

    // TC-ORD-047 / TC-BOX-009: 単一末端でも走査・正規化が縮退しない
    @Test
    fun singleLeafHierarchyWorks() {
        assertEquals(listOf("Only"), Solo.Enumish.entries.map { it.label })
        assertSame(Solo.Only, Solo.Enumish.valueOf("Only"))
    }

    // TC-ORD-060: 継承者ゼロの中間 sealed は空リストに展開され、entries に現れず valueOf でも解決されない
    @Test
    fun emptyIntermediateSealedContributesNothing() {
        assertEquals(listOf("A"), WithEmptyMid.Enumish.entries.map { it.label })
        assertFailsWith<IllegalArgumentException> { WithEmptyMid.Enumish.valueOf("None") }
    }
}
