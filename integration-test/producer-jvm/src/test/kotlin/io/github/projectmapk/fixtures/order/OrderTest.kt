package io.github.projectmapk.fixtures.order

import io.github.projectmapk.fixtures.bounds.Empty
import io.github.projectmapk.fixtures.bounds.Solo
import io.github.projectmapk.fixtures.bounds.WithEmptyMid
import io.github.projectmapk.fixtures.order.flat.A1
import io.github.projectmapk.fixtures.order.flat.AA
import io.github.projectmapk.fixtures.order.flat.AB1
import io.github.projectmapk.fixtures.order.flat.A_
import io.github.projectmapk.fixtures.order.flat.Ab2
import io.github.projectmapk.fixtures.order.flat.Az
import io.github.projectmapk.fixtures.order.flat.Box
import io.github.projectmapk.fixtures.order.flat.FlatRoot
import io.github.projectmapk.fixtures.order.flat.Foo
import io.github.projectmapk.fixtures.order.flat.FooBar
import io.github.projectmapk.fixtures.order.flat.Mmm
import io.github.projectmapk.fixtures.order.flat.Sep
import io.github.projectmapk.fixtures.order.flat.Sep0
import io.github.projectmapk.fixtures.order.flat.Zzz as FlatZzz
import io.github.projectmapk.fixtures.order.flat.aB3
import io.github.projectmapk.fixtures.order.flat.aLower
import io.github.projectmapk.fixtures.order.flat.ab4
import io.github.projectmapk.fixtures.order.mid.Aaa
import io.github.projectmapk.fixtures.order.mid.Early
import io.github.projectmapk.fixtures.order.mid.Late
import io.github.projectmapk.fixtures.order.mid.MidMulti
import io.github.projectmapk.fixtures.order.mid.MidNest
import io.github.projectmapk.fixtures.order.mid.MidRoot
import io.github.projectmapk.fixtures.order.mid.Outpost
import io.github.projectmapk.fixtures.order.mid.Probe
import io.github.projectmapk.fixtures.order.mid.Wide
import io.github.projectmapk.fixtures.order.mid.Zzz as MidZzz
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

// entries 順序の正典スナップショット（docs/test/ケース03-順序.md ORD-01〜ORD-03・ORD-05・ORD-06・ORD-09。
// 期待列は同資料 §2 の導出どおり。inheritors の観測はテスト限定の kotlin-reflect sealedSubclasses を使う）
class OrderTest {
    // ケース03 §2.1 の期待 entries（label 列）
    private val flatLabels =
        listOf(
            "A1",
            "AA",
            "AB1",
            "A_",
            "Ab2",
            "Az",
            "Bbb",
            "Aaa",
            "Foo",
            "FooBar",
            "Mmm",
            "Q",
            "Sep0",
            "Zzz",
            "aB3",
            "aLower",
            "ab4",
        )

    // ケース03 §2.2 の期待 entries（DFS 入れ子展開の label 列）
    private val midDfsLabels =
        listOf(
            "Aaa",
            "Bottom",
            "Wide",
            "NestA",
            "NestB",
            "Outpost",
            "Early",
            "Late",
            "Probe",
            "Bb",
            "Zzz",
        )

    private data class FlatSnapshot(
        val labels: List<String>,
        val leaves: List<KClass<out FlatRoot>>,
    )

    private data class KindOrderSnapshot(
        val inheritors: List<KClass<*>>,
        val entryKinds: List<KClass<*>>,
    )

    private data class BoundsSnapshot(
        val empty: List<String>,
        val solo: List<String>,
        val withEmptyMid: List<String>,
    )

    // docs/test/ケース03-順序.md ORD-01: FQN UTF-16 序数の全文字境界（大小区別・数字<大文字<'_'<小文字・
    // '.'(46)<'0'(48)・接頭辞先行）と配置 4 形を 1 スナップショットで固定。enumizedClass 列も同順
    @Test
    fun flatEntriesFollowFqnUtf16Ordinals() {
        val expected =
            FlatSnapshot(
                labels = flatLabels,
                leaves =
                    listOf(
                        A1::class,
                        AA::class,
                        AB1::class,
                        A_::class,
                        Ab2::class,
                        Az::class,
                        Box.Bbb::class,
                        FlatRoot.Aaa::class,
                        Foo::class,
                        FooBar::class,
                        Mmm::class,
                        Sep.Q::class,
                        Sep0::class,
                        FlatZzz::class,
                        aB3::class,
                        aLower::class,
                        ab4::class,
                    ),
            )
        val entries = FlatRoot.Enumish.entries
        assertEquals(
            expected,
            FlatSnapshot(entries.map { it.label }, entries.map { it.enumizedClass }),
        )
    }

    // docs/test/ケース03-順序.md ORD-02: §2.1 の列は宣言順・単純名昇順・大小無視順のいずれとも一致しない
    @Test
    fun flatOrderRejectsRivalOrderings() {
        val labels = FlatRoot.Enumish.entries.map { it.label }
        // 宣言順: FlatRoot.kt 内の同一ファイル群は Az → A_ → AA → A1 の順に宣言している
        val declaredGlyphs = listOf("Az", "A_", "AA", "A1")
        assertNotEquals(declaredGlyphs, labels.filter { it in declaredGlyphs })
        assertNotEquals(labels.sorted(), labels)
        assertNotEquals(labels.sortedBy { it.lowercase() }, labels)
    }

    // docs/test/ケース03-順序.md ORD-03: 中間 sealed はその位置で深さ優先に入れ子展開され
    // （ネスト末端=局所 FQN 一致・トップレベル末端=break・多段深部 break・基底ネスト中間の位置展開）、
    // 全末端 FQN 一括整列とは一致しない
    @Test
    fun midEntriesExpandIntermediatesDepthFirstInPlace() {
        val labels = MidRoot.Enumish.entries.map { it.label }
        assertEquals(midDfsLabels, labels)
        // 全末端 FQN 一括整列との対比（MidTop 配下と Wide の break を示す）
        val bulkSorted =
            listOf(
                "Aaa",
                "Early",
                "Late",
                "Bottom",
                "NestA",
                "NestB",
                "Outpost",
                "Probe",
                "Bb",
                "Wide",
                "Zzz",
            )
        assertNotEquals(bulkSorted, labels)
    }

    // docs/test/ケース03-順序.md ORD-05: 中間無しなら entries の enumizedClass 列 = 基底の sealedSubclasses・
    // 中間有りなら集合も並びも不一致（entries は末端までフラット化される）
    @Test
    fun sealedSubclassesMatchEntriesOnlyWithoutIntermediates() {
        assertEquals(
            FlatRoot.Enumish.entries.map { it.enumizedClass },
            FlatRoot::class.sealedSubclasses,
        )
        val midLeaves = MidRoot.Enumish.entries.map { it.enumizedClass }
        val midDirect = MidRoot::class.sealedSubclasses
        assertNotEquals(midLeaves, midDirect)
        assertNotEquals(midLeaves.toSet(), midDirect.toSet())
    }

    // docs/test/ケース03-順序.md ORD-06: フラット兄弟配置では生成 Enumish の inheritors 順
    // （kind ClassId の FQN 正規化。class 末端 Foo は Foo.Companion でも '.'(46) < 'B'(66) で先行が保たれる）が
    // entries の kind 列と一致する
    @Test
    fun inheritorsOrderMatchesEntriesInFlatSiblingLayout() {
        assertEquals(
            FlatRoot.Enumish.entries.map { it::class },
            FlatRoot.Enumish::class.sealedSubclasses,
        )
    }

    // docs/test/ケース03-順序.md ORD-06: class 末端の内側ネスト末端 Probe.Bb（'B'=66 < "Companion" の 'C'=67）で
    // inheritors 順（kind ClassId 整列）と entries 順（末端 ClassId 由来）の相対順が分岐する
    @Test
    fun inheritorsOrderDivergesForNestedLeafInsideClassLeaf() {
        val expected =
            KindOrderSnapshot(
                inheritors =
                    listOf(
                        Aaa::class,
                        Early::class,
                        Late::class,
                        MidMulti.Deep.Bottom::class,
                        MidNest.NestA::class,
                        MidNest.NestB::class,
                        Outpost::class,
                        Probe.Bb::class,
                        Probe.Companion::class,
                        Wide::class,
                        MidZzz::class,
                    ),
                entryKinds =
                    listOf(
                        Aaa::class,
                        MidMulti.Deep.Bottom::class,
                        Wide::class,
                        MidNest.NestA::class,
                        MidNest.NestB::class,
                        Outpost::class,
                        Early::class,
                        Late::class,
                        Probe.Companion::class,
                        Probe.Bb::class,
                        MidZzz::class,
                    ),
            )
        assertEquals(
            expected,
            KindOrderSnapshot(
                MidRoot.Enumish::class.sealedSubclasses,
                MidRoot.Enumish.entries.map { it::class },
            ),
        )
    }

    // docs/test/ケース03-順序.md ORD-09: 継承者ゼロ（空 entries）・単一末端・継承者ゼロ中間の空展開でも
    // entries は well-defined
    @Test
    fun lowerBoundHierarchiesKeepWellDefinedEntries() {
        val expected =
            BoundsSnapshot(empty = emptyList(), solo = listOf("Only"), withEmptyMid = listOf("A"))
        assertEquals(
            expected,
            BoundsSnapshot(
                Empty.Enumish.entries.map { it.label },
                Solo.Enumish.entries.map { it.label },
                WithEmptyMid.Enumish.entries.map { it.label },
            ),
        )
    }
}
