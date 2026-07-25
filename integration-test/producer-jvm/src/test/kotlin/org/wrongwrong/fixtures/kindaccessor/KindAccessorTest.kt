package org.wrongwrong.fixtures.kindaccessor

import kotlin.test.Test
import kotlin.test.assertEquals

// 参照不能 kind が IR-only アクセサ経由で entries / valueOf に載ることの実挙動
// （docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3）
class KindAccessorTest {
    @Test
    fun inaccessibleKindsAreServedThroughEntries() {
        assertEquals(listOf("KaPrivComp", "Visible"), KaRoot.Enumish.entries.map { it.label })
        assertEquals("KaPrivComp", KaRoot.Enumish.valueOf("KaPrivComp").label)
    }
}
