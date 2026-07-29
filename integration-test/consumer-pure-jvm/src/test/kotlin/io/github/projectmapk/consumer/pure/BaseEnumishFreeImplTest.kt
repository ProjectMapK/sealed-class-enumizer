package io.github.projectmapk.consumer.pure

import io.github.projectmapk.fixtures.si.SI
import io.github.projectmapk.sealedClassEnumizer.Enumish
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// 基底 Enumish の自由実装が跨 module で無制約なことの観測（docs/test/ケース05-境界横断.md XMP-26）。
// sealed 制約の対象は生成 Enumish のみで基底 Enumish には及ばない
class BaseEnumishFreeImplTest {
    // docs/test/ケース05-境界横断.md XMP-26: MyThing（このモジュールの自由実装）は SI 階層の kind では
    // ないため、enumishCompanion に SI 階層のものを借用していても entries / valueOf には現れない
    // （enum 的 API の保証は kind に閉じる）
    @Test
    fun baseEnumishImplementationIsUnrestricted() {
        val thing: Enumish = MyThing
        assertEquals("MyThing", thing.label)
        assertEquals(listOf("Bar", "Foo"), SI.Enumish.entries.map { it.label })
        assertNull(SI.Enumish.valueOfOrNull("MyThing"))
        assertSame(SI.Enumish.entries, thing.enumishCompanion.entries)
    }
}
