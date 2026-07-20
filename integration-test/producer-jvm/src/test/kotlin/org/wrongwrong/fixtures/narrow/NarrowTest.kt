package org.wrongwrong.fixtures.narrow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// internal 一色の階層と基底内ネスト末端の規則 1 box テスト
// （docs/テストケース管理.md TC-VIS-029 / TC-VIS-060 / TC-VIS-058）
class NarrowTest {
    // TC-VIS-029 / TC-VIS-060: eff(C) = eff(L) = eff(基底) = internal → 規則 1。
    // asEnumish() を具体型（NarrowLeaf.Companion）で受けられること自体が返り値型の検査
    @Test
    fun allInternalHierarchyUsesConcreteReturnType() {
        val kind: NarrowLeaf.Companion = NarrowLeaf(1).asEnumish()
        assertSame(NarrowLeaf.Companion, kind)
        assertEquals(listOf("NarrowLeaf"), NarrowBase.Enumish.entries.map { it.label })
    }

    // TC-VIS-058: 基底内ネストの public 宣言末端は eff = min(public, internal) = internal で
    // 「基底より広い末端」に該当せず、規則 1（具体型）のまま
    @Test
    fun nestedLeafOfInternalBaseStaysOnRule1() {
        val kind: NestHost.Inner.Companion = NestHost.Inner(1).asEnumish()
        assertSame(NestHost.Inner.Companion, kind)
        assertEquals("Inner", kind.label)
    }
}
