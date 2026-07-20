package org.wrongwrong.fixtures

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

// 生成 API の静的型（共変 override）の box テスト
// （docs/テストケース管理.md TC-LEAF-013 / TC-LEAF-014 / TC-LEAF-015 / TC-LEAF-018 /
//  TC-BOX-036 / TC-BOX-039 / TC-BOX-075）
class SIStaticTypingTest {
    // TC-LEAF-013: 規則 1 では asEnumish() の宣言返り値型は具体型（SI.Foo.Companion）。
    // 具体型の変数で受けられること自体が検査
    @Test
    fun asEnumishReturnsConcreteCompanionType() {
        val kind: SI.Foo.Companion = SI.Foo(1).asEnumish()
        assertSame(SI.Foo.Companion, kind)
    }

    // TC-LEAF-014: enumishCompanion は SI.Enumish.Companion へ共変 override され、
    // entries は List<SI.Enumish> として型付く
    @Test
    fun enumishCompanionIsCovariantlyTyped() {
        val si: SI = SI.Foo(1)
        val companion: SI.Enumish.Companion = si.asEnumish().enumishCompanion
        val entries: List<SI.Enumish> = companion.entries
        assertSame(SI.Enumish, companion)
        assertEquals(2, entries.size)
    }

    // TC-LEAF-015: どの kind から取っても enumishCompanion は同一の Companion シングルトン
    @Test
    fun enumishCompanionIsSharedAcrossKinds() {
        assertSame(SI.Foo.Companion.enumishCompanion, SI.Bar.enumishCompanion)
        assertSame(SI.Enumish, SI.Foo.Companion.enumishCompanion)
    }

    // TC-LEAF-018 / TC-BOX-036: enumizedClass の共変 override 連鎖により
    // entries.map{enumizedClass} は List<KClass<out SI>> として型付く
    @Test
    fun enumizedClassChainIsCovariantlyTyped() {
        val classes: List<KClass<out SI>> = SI.Enumish.entries.map { it.enumizedClass }
        assertEquals(listOf(SI.Bar::class, SI.Foo::class), classes)
    }

    // TC-BOX-039: kind 経由の entries は階層の entries と同一インスタンスで List<SI.Enumish> に型付く
    @Test
    fun entriesViaKindAreSameInstanceAndTyped() {
        val viaKind: List<SI.Enumish> = SI.Bar.enumishCompanion.entries
        assertSame(SI.Enumish.entries, viaKind)
    }

    // TC-BOX-075: class 末端の kind（companion）は SI.Enumish であって SI ではない。
    // object 末端は値 = kind のため SI と SI.Enumish の両方である
    @Test
    fun companionKindIsNotAValueOfTheHierarchy() {
        val fooKind: Any = SI.Foo.Companion
        val bar: Any = SI.Bar
        assertTrue(fooKind is SI.Enumish)
        assertFalse(fooKind is SI)
        assertTrue(bar is SI)
        assertTrue(bar is SI.Enumish)
    }
}
