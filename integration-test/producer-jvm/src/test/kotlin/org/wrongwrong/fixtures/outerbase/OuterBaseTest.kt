package org.wrongwrong.fixtures.outerbase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

// class 内にネストした基底の box テスト
// （docs/テストケース管理.md TC-VIS-005 / TC-VIS-006 / TC-BOX-078）
class OuterBaseTest {
    // TC-VIS-005: 多段ネスト生成（Outer.SI.Enumish とその Companion）が成立し全 API が動作する
    @Test
    fun nestedBaseGeneratesNestedApi() {
        assertEquals(listOf("A", "B"), Outer.SI.Enumish.entries.map { it.label })
        assertSame(Outer.SI.A, Outer.SI.Enumish.valueOf("A"))
        assertSame(Outer.SI.B.Companion, Outer.SI.B(1).asEnumish())
    }

    // TC-BOX-078: 外側クラスにネストした基底でも valueOf の失敗メッセージは simpleName のみ（"Outer.SI" でない）
    @Test
    fun failureMessageUsesSimpleNameOnly() {
        val failure = assertFailsWith<IllegalArgumentException> { Outer.SI.Enumish.valueOf("X") }
        assertEquals("No enumish entry with label 'X' in SI", failure.message)
    }

    // TC-VIS-006: private ネスト基底は外側クラス本体内（可視範囲の内側）で全 API + else 無し when が使える
    @Test
    fun privateNestedBaseWorksInsideOuterBody() {
        val outer = Outer2()
        assertEquals(listOf("X", "Y"), outer.labels())
        assertEquals("X", outer.resolve("X"))
        assertEquals(listOf("x", "y"), listOf(outer.pick(true), outer.pick(false)))
    }
}
