package org.wrongwrong.fixtures.guarded

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.wrongwrong.sealedClassEnumizer.label

// sealed class 基底 + protected ネスト末端の box テスト（docs/テストケース管理.md TC-VIS-017）
class GuardedTest {
    // protected 末端は基底本体から参照可能で（直接参照）、entries に載る
    @Test
    fun protectedNestedLeafAppearsInEntries() {
        assertEquals(listOf("Inner", "Open"), Guarded.Enumish.entries.map { it.label })
        assertNotNull(Guarded.Enumish.valueOfOrNull("Inner"))
    }

    // protected 末端の値も通常どおり kind 解決される（値は基底の factory 経由で得る）
    @Test
    fun protectedLeafValueResolvesToItsKind() {
        val inner: Guarded = Guarded.makeInner()
        assertEquals("Inner", inner.label)
        assertEquals("Inner", inner.asEnumish().label)
    }
}
