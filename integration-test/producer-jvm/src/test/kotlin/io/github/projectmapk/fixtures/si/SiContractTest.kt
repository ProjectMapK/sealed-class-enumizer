package io.github.projectmapk.fixtures.si

import io.github.projectmapk.fixtures.enumleaf.Command
import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// SI の API 契約全面（docs/test/ケース01-生成と実行時API.md §1）
class SiContractTest {
    // docs/test/ケース01-生成と実行時API.md API-01: 値→kind の全経路（asEnumish / entries / valueOf）が
    // 同一シングルトンへ収束する。class 系は companion・object は自身が kind
    @Test
    fun entriesHoldKindSingletons() {
        assertEquals(listOf("Bar", "Foo"), SI.Enumish.entries.map { it.label })
        assertSame(SI.Bar, SI.Enumish.entries[0])
        assertSame(SI.Foo.Companion, SI.Enumish.entries[1])
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        // 異なるインスタンスからも単一 kind へ収束する
        assertSame(SI.Foo(1).asEnumish(), SI.Foo(2).asEnumish())
        val bar: SI = SI.Bar
        assertSame(SI.Bar, bar.asEnumish())
    }

    // docs/test/ケース01-生成と実行時API.md API-02: entries は毎回同一 List 参照（lazy 一度・memoize）
    @Test
    fun entriesAreMemoized() {
        assertSame(SI.Enumish.entries, SI.Enumish.entries)
    }

    // docs/test/ケース01-生成と実行時API.md API-03: 完全一致のみ解決し、失敗は
    // IllegalArgumentException（文言は simpleName）・valueOfOrNull は null
    @Test
    fun valueOfContractAndFailureMessage() {
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        // 部分一致・大小無視・空・空白は全て不一致
        assertEquals(
            listOf(null, null, null, null),
            listOf("Fo", "foo", "", " ").map { SI.Enumish.valueOfOrNull(it) },
        )
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("Nope") }
        assertEquals("No enumish entry with label 'Nope' in SI", failure.message)
        assertNull(SI.Enumish.valueOfOrNull("Nope"))
    }

    // docs/test/ケース01-生成と実行時API.md API-04: 全 entries で valueOf(label) が往復一致し、
    // enumishCompanion 経由でも同一
    @Test
    fun valueOfRoundTripsAllEntries() {
        val entries = SI.Enumish.entries
        assertEquals(entries, entries.map { SI.Enumish.valueOf(it.label) })
        assertEquals(entries, entries.map { it.enumishCompanion.valueOf(it.label) })
    }

    // docs/test/ケース01-生成と実行時API.md API-05: label 拡張は kind の label を返す。
    // 確実な取得経路は asEnumish().label
    @Test
    fun labelExtensionReadsKindLabel() {
        val foo: SI = SI.Foo(42)
        val bar: SI = SI.Bar
        assertEquals(listOf("Foo", "Bar"), listOf(foo.label, bar.label))
        assertEquals(foo.label, foo.asEnumish().label)
    }

    // docs/test/ケース01-生成と実行時API.md API-06: enumizedClass は末端の KClass で、
    // 共変連鎖が List<KClass<out SI>> に型付く
    @Test
    fun enumizedClassChainIsCovariant() {
        val classes: List<KClass<out SI>> = SI.Enumish.entries.map { it.enumizedClass }
        assertEquals(listOf(SI.Bar::class, SI.Foo::class), classes)
    }

    // docs/test/ケース01-生成と実行時API.md API-07: enumishCompanion は全 kind で同一シングルトンを返し、
    // 共変 override により entries は List<SI.Enumish> に型付く
    @Test
    fun enumishCompanionIsSharedAndCovariant() {
        val companion: SI.Enumish.Companion = SI.Foo(1).asEnumish().enumishCompanion
        val entries: List<SI.Enumish> = companion.entries
        assertSame(SI.Enumish, companion)
        assertSame(SI.Bar.enumishCompanion, companion)
        assertSame(SI.Enumish.entries, entries)
    }

    // docs/test/ケース01-生成と実行時API.md API-08: 規則 1 では asEnumish() の返り値型は
    // 具体型 companion 型。具体型の変数で受けられること自体が検査（規則 2/3 はケース02）
    @Test
    fun asEnumishReturnsConcreteCompanionType() {
        val kind: SI.Foo.Companion = SI.Foo(1).asEnumish()
        assertSame(SI.Foo.Companion, kind)
    }

    // docs/test/ケース01-生成と実行時API.md API-09: kind-when は同一 module で else 省略が成立し、
    // 枝形 3 種（companion 等値・短縮形・is）が動作する。kind-when は値型を絞らず値 when はスマートキャスト
    @Test
    fun whenBranchShapesAndExhaustiveness() {
        fun byEquality(si: SI): String =
            when (si.asEnumish()) {
                SI.Foo.Companion -> "foo"
                SI.Bar -> "bar"
            }

        fun byShortForm(si: SI): String =
            when (si.asEnumish()) {
                SI.Foo -> "foo"
                SI.Bar -> "bar"
            }

        fun byIsBranch(si: SI): String =
            when (si.asEnumish()) {
                is SI.Foo.Companion -> "foo"
                is SI.Bar -> "bar"
            }

        val values = listOf<SI>(SI.Foo(1), SI.Bar)
        assertEquals(listOf("foo", "bar"), values.map(::byEquality))
        assertEquals(listOf("foo", "bar"), values.map(::byShortForm))
        assertEquals(listOf("foo", "bar"), values.map(::byIsBranch))

        // 値 when はスマートキャストが効き else 不要（sealed の地力）
        val si: SI = SI.Foo(41)
        val smartCast =
            when (si) {
                is SI.Foo -> si.v + 1
                SI.Bar -> 0
            }
        assertEquals(42, smartCast)

        // kind-when では値の型は絞り込まれない（分岐内でも si は SI のまま。member v へは is 判定が必要）
        val narrowed =
            when (si.asEnumish()) {
                SI.Foo.Companion -> si is SI.Foo
                SI.Bar -> false
            }
        assertTrue(narrowed)
    }

    // docs/test/ケース01-生成と実行時API.md API-10: companion kind は SI.Enumish であって
    // SI の値ではない。object 末端は値 = kind のため両方である
    @Test
    fun kindIsNotAHierarchyValue() {
        val fooKind: Any = SI.Foo.Companion
        val bar: Any = SI.Bar
        assertTrue(fooKind is SI.Enumish)
        assertFalse(fooKind is SI)
        assertTrue(bar is SI)
        assertTrue(bar is SI.Enumish)
    }

    // docs/test/ケース01-生成と実行時API.md API-11: 複数階層の Companion を
    // List<EnumishCompanion<Enumish>> へ射影なしで束ね各 entries を取得できる（out 共変。si + enumleaf）
    @Test
    fun companionsBundleWithoutProjection() {
        val companions: List<EnumishCompanion<Enumish>> = listOf(SI.Enumish, Command.Enumish)
        val labels = companions.flatMap { companion -> companion.entries.map { it.label } }
        assertEquals(listOf("Bar", "Foo", "Builtin", "Custom", "HELP", "Verb"), labels)
    }

    // docs/test/ケース01-生成と実行時API.md API-36: data object は言語合成 toString のまま非生成。
    // data class 値インスタンスの data 合成 toString も kind 側と管轄分離で不変
    @Test
    fun dataObjectKeepsSynthesizedToString() {
        assertEquals(
            listOf("Bar", "Foo(v=1)", "Foo"),
            listOf(SI.Bar.toString(), SI.Foo(1).toString(), SI.Foo.Companion.toString()),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-50: 生成 Enumish・kind は ordinal 相当メンバー・
    // Comparable を提供しない（enum との意図的差異。順序取得は entries 限定 = 序数永続化禁止の根拠）。
    // NG: 次はいずれもコンパイル不能
    //   SI.Foo.Companion.ordinal                                  // e: unresolved reference:
    // ordinal
    //   SI.Enumish.valueOf("Bar") < SI.Enumish.valueOf("Foo")     // e: Comparable ではないため比較不能
    @Test
    fun kindsExposeNoOrdinalOrComparable() {
        val kinds: List<Any> = SI.Enumish.entries
        assertEquals(emptyList(), kinds.filter { it is Comparable<*> })
    }
}
