package io.github.projectmapk.mpp.consumer

import io.github.projectmapk.mpp.fixtures.wider.Wide
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 基底（internal）より広い public 末端の外部観測面（E-2 の MPP 版。docs/test/ケース05-境界横断.md
// XMP-43・docs/エッジケースへの対応方針.md §1.2）。階層 API（entries / valueOf / WideBase.Enumish の名指し）は
// internal 基底ゆえ本モジュールから不可視であり、値・kind API のみが利用できる
class ConsumerWiderLeafTest {
    // 値・kind API（asEnumish / label / enumizedClass）は public 末端経由で利用できる
    @Test
    fun valueAndKindApiUsableExternally() {
        val kind = Wide().asEnumish()
        assertSame(Wide.Companion, kind)
        assertEquals(listOf("Wide", "Wide"), listOf(kind.label, kind.enumizedClass.simpleName))
    }

    // label 拡張プロパティも public 末端の値に対して動作する
    @Test
    fun labelExtensionWorksOnWiderLeaf() {
        assertEquals("Wide", Wide().label)
    }
}
