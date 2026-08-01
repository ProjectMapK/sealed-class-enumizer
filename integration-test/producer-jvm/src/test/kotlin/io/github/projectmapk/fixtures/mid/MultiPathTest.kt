package io.github.projectmapk.fixtures.mid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

// 合流（複数経路で到達するメンバー）の box テスト（docs/test/ケース01-生成と実行時API.md §7）
class MultiPathTest {
    // docs/test/ケース01-生成と実行時API.md API-57: 複数経路で到達するメンバーは初出位置に 1 回だけ載る。
    // 基底の直接継承者 [MpClassMid, MpDirect, MpLeft, MpRight] を DFS 展開すると
    // MpClassMid → [MpMixed] / MpDirect（基底直下）/
    // MpLeft → [MpBoth, MpHost.Companion, MpNested, MpOnlyLeft, MpShared → [MpDeep, MpNested=重複]] /
    // MpRight → [MpBoth, MpDirect, MpHost.Companion, MpMixed, MpOnlyRight, MpShared=重複]
    // となり、2 度目の到達は末端・中間 sealed のサブツリーとも落ちる（docs/test/ケース03-順序.md §1）
    @Test
    fun multiPathLeafIsListedOnceAtFirstOccurrence() {
        val labels = MultiPath.Enumish.entries.map { it.label }
        assertEquals(
            listOf(
                "MpMixed",
                "MpDirect",
                "MpBoth",
                "Companion",
                "MpNested",
                "MpOnlyLeft",
                "MpDeep",
                "MpOnlyRight",
            ),
            labels,
        )
        // 初出位置は経路の到達順が決めるため、全末端 FQN 一括整列とは一致しない
        assertNotEquals(labels.sorted(), labels)
    }

    // docs/test/ケース01-生成と実行時API.md API-57: 経路が複数でも kind は 1 つで、
    // どの経路の静的型からも同じ kind へ解決される。生成 Enumish の継承者一覧も重複せず
    // kind-when は else 不要で網羅する
    @Test
    fun multiPathLeafHasSingleKind() {
        // 合流点 = 非 sealed 末端（兄弟中間の対称経路）
        val bothViaLeft: MpLeft = MpBoth()
        val bothViaRight: MpRight = MpBoth()
        assertSame(MpBoth.Companion, bothViaLeft.asEnumish())
        assertSame(MpBoth.Companion, bothViaRight.asEnumish())
        assertSame(MpBoth.Companion, MultiPath.Enumish.valueOf("MpBoth"))
        // 合流点 = 中間 sealed（配下末端はどちらの経路から見ても同じ kind）
        val deepViaLeft: MpLeft = MpDeep
        val deepViaRight: MpRight = MpDeep
        assertSame(MpDeep, deepViaLeft.asEnumish())
        assertSame(MpDeep, deepViaRight.asEnumish())
        // 合流点 = companion 末端（外側 MpHost は階層外）
        assertSame(MpHost.Companion, MultiPath.Enumish.valueOf("Companion"))
        // 経路の非対称（基底直下 + 中間経由）
        val directViaBase: MultiPath = MpDirect
        val directViaRight: MpRight = MpDirect
        assertSame(MpDirect, directViaBase.asEnumish())
        assertSame(MpDirect, directViaRight.asEnumish())
        // 経路の混成（sealed class 中間 + sealed interface 中間）
        val mixedViaClass: MpClassMid = MpMixed()
        val mixedViaIface: MpRight = MpMixed()
        assertSame(MpMixed.Companion, mixedViaClass.asEnumish())
        assertSame(MpMixed.Companion, mixedViaIface.asEnumish())
        // 合流の入れ子（合流点の配下でさらに交差する末端）
        val nestedViaShared: MpShared = MpNested()
        val nestedViaLeft: MpLeft = MpNested()
        assertSame(MpNested.Companion, nestedViaShared.asEnumish())
        assertSame(MpNested.Companion, nestedViaLeft.asEnumish())
        assertEquals(
            listOf(
                "mixed-mid",
                "base-direct",
                "iface-pair",
                "companion",
                "nested",
                "left-only",
                "shared-mid",
                "right-only",
            ),
            MultiPath.Enumish.entries.map { kind ->
                when (kind) {
                    MpMixed.Companion -> "mixed-mid"
                    MpDirect -> "base-direct"
                    MpBoth.Companion -> "iface-pair"
                    MpHost.Companion -> "companion"
                    MpNested.Companion -> "nested"
                    MpOnlyLeft -> "left-only"
                    MpDeep -> "shared-mid"
                    MpOnlyRight -> "right-only"
                }
            },
        )
    }
}
