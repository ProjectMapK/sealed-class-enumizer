package org.wrongwrong.consumer.pure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.wrongwrong.fixtures.SI
import org.wrongwrong.sealedClassEnumizer.Enumish

// 基底 Enumish の手動実装が跨モジュールで無制約なことの観測（docs/概要.md §8 許容・
// docs/テストケース管理.md TC-XM-045 / TC-MAN-009）。sealed 制約の対象は生成 Enumish のみで
// 基底 Enumish には及ばない
class BaseEnumishFreeImplTest {
    // MyThing（このモジュールの手動実装）は SI 階層と無関係で、entries / valueOf には一切現れない
    // （enum 的 API の保証は kind に閉じる）
    @Test
    fun baseEnumishImplementationIsUnrestrictedAcrossModule() {
        val thing: Enumish = MyThing
        assertEquals("MyThing", thing.label)
        assertEquals(listOf("Bar", "Foo"), SI.Enumish.entries.map { it.label })
        assertNull(SI.Enumish.valueOfOrNull("MyThing"))
        assertSame(SI.Enumish.entries, thing.enumishCompanion.entries)
    }
}
