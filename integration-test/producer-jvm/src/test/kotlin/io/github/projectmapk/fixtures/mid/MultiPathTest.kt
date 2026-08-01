package io.github.projectmapk.fixtures.mid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 多重経路（複数の中間 sealed を同時に実装する末端）の box テスト
// （docs/test/ケース01-生成と実行時API.md §7）
class MultiPathTest {
    // docs/test/ケース01-生成と実行時API.md API-57: 複数経路で到達する末端は初出位置に 1 回だけ載る。
    // 直接継承者の整列 [MpLeft, MpRight] を DFS 展開すると MpLeft 側で [MpBoth, MpOnlyLeft]・
    // MpRight 側で [MpBoth（重複）, MpOnlyRight] となり、2 度目の MpBoth は落ちる
    @Test
    fun multiPathLeafIsListedOnceAtFirstOccurrence() {
        assertEquals(
            listOf("MpBoth", "MpOnlyLeft", "MpOnlyRight"),
            MultiPath.Enumish.entries.map { it.label },
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-57: 経路が複数でも kind は 1 つで、
    // どちらの中間型からも同じ kind へ解決される。生成 Enumish の継承者一覧も重複せず
    // kind-when は else 不要で網羅する
    @Test
    fun multiPathLeafHasSingleKind() {
        val viaLeft: MpLeft = MpBoth()
        val viaRight: MpRight = MpBoth()
        assertSame(MpBoth.Companion, viaLeft.asEnumish())
        assertSame(MpBoth.Companion, viaRight.asEnumish())
        assertSame(MpBoth.Companion, MultiPath.Enumish.valueOf("MpBoth"))
        assertEquals(
            listOf("both", "left", "right"),
            MultiPath.Enumish.entries.map { kind ->
                when (kind) {
                    MpBoth.Companion -> "both"
                    MpOnlyLeft -> "left"
                    MpOnlyRight -> "right"
                }
            },
        )
    }
}
