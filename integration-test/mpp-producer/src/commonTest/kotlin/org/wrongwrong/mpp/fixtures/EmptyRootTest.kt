package org.wrongwrong.mpp.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// 空階層（末端ゼロ）の MPP 観測（docs/テストケース管理.md TC-GAP-018 の producer 側）。
// 全 platform で entries = [] が一致し、valueOf は常に失敗メッセージ付きの例外になる
class EmptyRootTest {
    @Test
    fun entriesIsEmptyOnEveryTarget() {
        assertEquals(emptyList(), EmptyRoot.Enumish.entries)
    }

    @Test
    fun valueOfAlwaysFailsWithUniformMessage() {
        val failure = assertFailsWith<IllegalArgumentException> { EmptyRoot.Enumish.valueOf("Any") }
        assertEquals("No enumish entry with label 'Any' in EmptyRoot", failure.message)
    }
}
