package org.wrongwrong.mpp.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// 空階層（末端ゼロ）の MPP 観測（docs/test/ケース05-境界横断.md XMP-08 の producer 側）。
// 全 platform で entries = [] が一致し、valueOf は常に失敗メッセージ付きの例外になる
class BoundsTest {
    @Test
    fun entriesIsEmptyOnEveryTarget() {
        assertEquals(emptyList(), EmptyRoot.Enumish.entries)
    }

    @Test
    fun valueOfAlwaysFailsUniformly() {
        val failure = assertFailsWith<IllegalArgumentException> { EmptyRoot.Enumish.valueOf("Any") }
        assertEquals("No enumish entry with label 'Any' in EmptyRoot", failure.message)
    }
}
