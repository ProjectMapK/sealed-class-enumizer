package org.wrongwrong.consumer.pure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.wrongwrong.fixtures.emptyhier.Empty
import org.wrongwrong.fixtures.midvis.ViaMid
import org.wrongwrong.fixtures.mixedorder.S
import org.wrongwrong.fixtures.nested.NestedRoot

// entries の順序・集合の跨モジュール観測（docs/概要.md §5・§7、docs/テストケース管理.md
// TC-ORD-040 / TC-XM-046 / TC-ORD-062 / TC-XM-047 / TC-GAP-016 / TC-GAP-018）
class CrossModuleOrderTest {
    // TC-ORD-040: 混在配置（ネスト・トップレベル・小文字始まり）の FQN 序数順が跨モジュールでも
    // 保存される（定義モジュールのコンパイル時に決定され、実行時にホルダーで解決される）
    @Test
    fun mixedPlacementEntriesKeepFqnOrderAcrossModule() {
        assertEquals(
            listOf("Bbb", "Mmm", "Aaa", "Zzz", "aLower"),
            S.Enumish.entries.map { it.label },
        )
    }

    // TC-XM-046 / TC-ORD-062: 中間 sealed の入れ子展開順（break 順序）が跨モジュールで保存される。
    // 末端集合を FQN 順に並べた [Aaa, Bbb] にはならず、label 探索（valueOf）は順序非依存で両方解決する
    @Test
    fun intermediateBreakOrderIsPreservedAcrossModule() {
        assertEquals(listOf("Bbb", "Aaa"), NestedRoot.Enumish.entries.map { it.label })
        assertEquals(
            listOf("Aaa", "Bbb"),
            listOf(NestedRoot.Enumish.valueOf("Aaa").label, NestedRoot.Enumish.valueOf("Bbb").label),
        )
    }

    // TC-XM-047: sealedSubclasses（直接の継承者・中間 sealed 含む）と entries（末端までフラット化）は
    // 要素の集合も並びも一致しない（docs/概要.md §3「リフレクション連携」。kotlin-reflect はテスト観測用）
    @Test
    fun entriesDifferFromSealedSubclassesAcrossModule() {
        val direct = NestedRoot::class.sealedSubclasses.map { it.simpleName ?: "" }
        val entryLabels = NestedRoot.Enumish.entries.map { it.label }
        assertEquals(listOf("Bbb", "Mid"), direct)
        assertEquals(listOf("Bbb", "Aaa"), entryLabels)
        assertNotEquals(direct.toSet(), entryLabels.toSet())
    }

    // TC-GAP-016（静的側）: internal 中間 sealed を経由する階層も跨モジュールで通常どおり観測できる
    // （中間には何も生成されず可視性も影響しない。IC での並び再配置検査は gradle-integration 担当）
    @Test
    fun internalIntermediateHierarchyIsObservableAcrossModule() {
        assertEquals(listOf("MA", "MB"), ViaMid.Enumish.entries.map { it.label })
    }

    // TC-GAP-018（JVM 跨モジュール面）: 空階層の entries は空・valueOf は常に失敗し文言も保たれる
    // （MPP common 面は mpp-* 担当）
    @Test
    fun emptyHierarchyIsObservableAcrossModule() {
        assertEquals(emptyList(), Empty.Enumish.entries.map { it.label })
        val failure =
            assertFailsWith<IllegalArgumentException> { Empty.Enumish.valueOf("Anything") }
        assertEquals("No enumish entry with label 'Anything' in Empty", failure.message)
    }
}
