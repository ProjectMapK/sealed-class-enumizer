package io.github.projectmapk.consumer.pure

import io.github.projectmapk.fixtures.bounds.Empty
import io.github.projectmapk.fixtures.order.flat.FlatRoot
import io.github.projectmapk.fixtures.order.mid.MidRoot
import io.github.projectmapk.fixtures.vis.pub.MA
import io.github.projectmapk.fixtures.vis.pub.VisRoot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

// entries の順序・集合の跨 module 観測（docs/test/ケース05-境界横断.md XMP-06〜XMP-08。
// 順序値の正典はケース03。期待列は producer 側フィクスチャのコメントおよび OrderTest と同一）
class CrossModuleOrderTest {
    // docs/test/ケース05-境界横断.md XMP-06: 混在配置 17 末端の FQN UTF-16 序数順が跨 module で保存される
    // （定義モジュールのコンパイル時に決定され、実行時にホルダーで解決される）
    @Test
    fun mixedPlacementKeepsFqnOrder() {
        assertEquals(
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
            ),
            FlatRoot.Enumish.entries.map { it.label },
        )
    }

    // docs/test/ケース05-境界横断.md XMP-06: 中間 sealed の DFS 入れ子展開順（break 込み）が
    // 跨 module で保存される。label 探索（valueOf）は順序非依存で break の両側を解決する
    @Test
    fun intermediateBreakOrderIsPreserved() {
        assertEquals(
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
            ),
            MidRoot.Enumish.entries.map { it.label },
        )
        assertEquals(
            listOf("Early", "Wide"),
            listOf(MidRoot.Enumish.valueOf("Early").label, MidRoot.Enumish.valueOf("Wide").label),
        )
    }

    // docs/test/ケース05-境界横断.md XMP-06: internal 中間 sealed（HiddenMid）経由の public 末端 MA も
    // 跨 module で通常どおり観測でき、FQN 位置（VisRoot.kt の期待列 index 9）に載る
    @Test
    fun internalIntermediateHierarchyIsObservable() {
        assertSame(MA, VisRoot.Enumish.valueOf("MA"))
        assertSame(MA, MA.asEnumish())
        assertEquals("MA", VisRoot.Enumish.entries[9].label)
    }

    // docs/test/ケース05-境界横断.md XMP-07: sealedSubclasses（直接継承者・中間 sealed 含む）と
    // entries（末端フラット化）は集合も並びも一致しない（kotlin-reflect はテスト観測用。
    // sealedSubclasses 自体の順序は言語保証外のため整列して比較する）
    @Test
    fun entriesDifferFromSealedSubclasses() {
        val direct = MidRoot::class.sealedSubclasses.map { it.simpleName ?: "" }
        val entryLabels = MidRoot.Enumish.entries.map { it.label }
        assertEquals(
            listOf("Aaa", "Bb", "MidIn", "MidMulti", "MidNest", "MidTop", "Probe", "Zzz"),
            direct.sorted(),
        )
        assertNotEquals(direct.toSet(), entryLabels.toSet())
    }

    // docs/test/ケース05-境界横断.md XMP-08: 空階層は entries = []・valueOf 常時失敗が跨 module でも
    // 保たれる（MPP 全ターゲット面は mpp-producer / mpp-consumer が担う）
    @Test
    fun emptyHierarchyIsObservable() {
        assertEquals(emptyList(), Empty.Enumish.entries.map { it.label })
        val failure =
            assertFailsWith<IllegalArgumentException> { Empty.Enumish.valueOf("Anything") }
        assertEquals("No enumish entry with label 'Anything' in Empty", failure.message)
    }
}
