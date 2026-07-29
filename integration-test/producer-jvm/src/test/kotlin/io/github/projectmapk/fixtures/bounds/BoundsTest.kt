package io.github.projectmapk.fixtures.bounds

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

// 空 / 単一境界の box 面（docs/test/ケース01-生成と実行時API.md §13。順序面はケース03 ORD-09）
class BoundsTest {
    // docs/test/ケース01-生成と実行時API.md API-48: 空階層は entries 空かつ memoize・
    // valueOf 常時例外＋文言・valueOfOrNull = null・診断なし
    @Test
    fun emptyHierarchyContract() {
        val entries = Empty.Enumish.entries
        assertEquals(emptyList(), entries)
        assertSame(entries, Empty.Enumish.entries)
        assertNull(Empty.Enumish.valueOfOrNull("Anything"))
        val failure =
            assertFailsWith<IllegalArgumentException> { Empty.Enumish.valueOf("Anything") }
        assertEquals("No enumish entry with label 'Anything' in Empty", failure.message)
    }

    // docs/test/ケース01-生成と実行時API.md API-49: 単一末端階層・継承者ゼロ中間（空展開）でも
    // 全 API が成立する
    @Test
    fun singleLeafHierarchyWorks() {
        assertEquals(listOf("Only"), Solo.Enumish.entries.map { it.label })
        assertSame(Solo.Only, Solo.Enumish.valueOf("Only"))
        assertSame(Solo.Only, Solo.Only.asEnumish())
        assertEquals(listOf("A"), WithEmptyMid.Enumish.entries.map { it.label })
        assertSame(WithEmptyMid.A, WithEmptyMid.Enumish.valueOf("A"))
        assertFailsWith<IllegalArgumentException> { WithEmptyMid.Enumish.valueOf("None") }
    }
}
