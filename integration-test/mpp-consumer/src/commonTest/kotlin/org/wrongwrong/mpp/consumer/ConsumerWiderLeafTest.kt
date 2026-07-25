package org.wrongwrong.mpp.consumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.mpp.fixtures.wider.Wide
import org.wrongwrong.sealedClassEnumizer.label

// 基底（internal）より広い public 末端の外部観測面（E-2 の MPP 版。docs/テストケース管理.md
// TC-GAP-017 の外部観測・エッジ §1.2）。階層 API（entries / valueOf / WideBase.Enumish の名指し）は
// internal 基底ゆえ本モジュールから不可視であり、値・kind API のみが利用できる
class ConsumerWiderLeafTest {
    // 値・kind API（asEnumish / label / enumizedClass）は public 末端経由で利用できる
    @Test
    fun valueAndKindApiAreUsableExternally() {
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
