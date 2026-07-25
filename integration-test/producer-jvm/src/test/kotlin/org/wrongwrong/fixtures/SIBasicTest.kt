package org.wrongwrong.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// 標準形 SI の box テスト（docs/テストケース管理.md H 軸の基本ケース）
class SIBasicTest {
    // TC-BOX-001: entries の内容・型・kind シングルトン性（FQN 昇順 = 単純名昇順）
    @Test
    fun entriesContainsKindSingletonsInFqnOrder() {
        val entries: List<SI.Enumish> = SI.Enumish.entries
        assertEquals(listOf("Bar", "Foo"), entries.map { it.label })
        assertSame(SI.Bar, entries[0])
        assertSame(SI.Foo.Companion, entries[1])
    }

    // TC-BOX-002: entries は毎回同じインスタンスを返す（遅延構築は一度きり）
    @Test
    fun entriesReturnsSameInstance() {
        assertSame(SI.Enumish.entries, SI.Enumish.entries)
    }

    // TC-BOX 系: valueOf / valueOfOrNull と失敗メッセージ（docs/概要.md §2 の文言）
    @Test
    fun valueOfResolvesByLabel() {
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        assertSame(SI.Bar, SI.Enumish.valueOfOrNull("Bar"))
        assertNull(SI.Enumish.valueOfOrNull("Nope"))
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("Nope") }
        assertEquals("No enumish entry with label 'Nope' in SI", failure.message)
    }

    // TC-LEAF 系: 値が別でも kind は同一シングルトン（末端 class は companion が kind）
    @Test
    fun asEnumishReturnsKindSingleton() {
        assertSame(SI.Foo(1).asEnumish(), SI.Foo(2).asEnumish())
        assertSame(SI.Foo.Companion, SI.Foo(1).asEnumish())
        val bar: SI = SI.Bar
        assertSame(SI.Bar, bar.asEnumish())
    }

    // label 拡張プロパティ（値から kind の label）
    @Test
    fun labelExtensionReadsKindLabel() {
        val foo: SI = SI.Foo(42)
        val bar: SI = SI.Bar
        assertEquals(listOf("Foo", "Bar"), listOf(foo.label, bar.label))
    }

    // enumizedClass は kind に対応する末端の class リテラル（docs/概要.md §2）
    @Test
    fun enumizedClassPointsToLeafClass() {
        assertEquals(SI.Foo::class, SI.Foo.Companion.enumizedClass)
        assertEquals(SI.Bar::class, SI.Bar.enumizedClass)
    }

    // enumishCompanion は値から階層全体へ辿る手段（enum の declaringClass 相当）
    @Test
    fun enumishCompanionNavigatesToHierarchyCompanion() {
        assertSame(SI.Enumish, SI.Bar.enumishCompanion)
        assertSame(SI.Enumish.entries, SI.Bar.enumishCompanion.entries)
    }

    // toString の 2 原則: 明示実装の無い kind には label を返す toString が生成される。
    // 末端 data object は言語合成（単純名）のまま（docs/概要.md §4）
    @Test
    fun toStringReturnsLabel() {
        assertEquals(listOf("Foo", "Bar"), listOf(SI.Foo.Companion.toString(), SI.Bar.toString()))
    }

    // kind 単位の網羅 when（else 省略 = 生成 Enumish の sealed 化 V1 の同一モジュール内実証）
    @Test
    fun kindWhenIsExhaustiveWithoutElse() {
        val si: SI = SI.Foo(1)
        val result =
            when (si.asEnumish()) {
                SI.Foo.Companion -> "foo"
                SI.Bar -> "bar"
            }
        assertEquals("foo", result)
    }
}
