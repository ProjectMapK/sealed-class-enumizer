package io.github.projectmapk.fixtures.manual.samelabel

import kotlin.test.Test
import kotlin.test.assertEquals

// 階層間 label 閉域の box テスト（docs/test/ケース01-生成と実行時API.md API-43。
// 同一階層内の衝突 LABEL_CLASH はケース04 が正典）
class SameLabelTest {
    private data class LeakSnapshot(
        val firstLabels: List<String>,
        val secondLabels: List<String>,
        val firstResolvesOwn: Boolean,
        val secondResolvesOwn: Boolean,
        val firstListsForeign: Boolean,
        val secondListsForeign: Boolean,
    )

    // docs/test/ケース01-生成と実行時API.md API-43: 独立 2 階層の同名末端 Same は各 valueOf が
    // 自階層のみ照合し、entries は独立・他階層 label へ漏出しない
    @Test
    fun hierarchiesDoNotLeak() {
        val firstSame: Any = FirstNs.Same
        val secondSame: Any = SecondNs.Same
        val expected =
            LeakSnapshot(
                firstLabels = listOf("Same"),
                secondLabels = listOf("Same"),
                firstResolvesOwn = true,
                secondResolvesOwn = true,
                firstListsForeign = false,
                secondListsForeign = false,
            )
        val actual =
            LeakSnapshot(
                firstLabels = FirstNs.Enumish.entries.map { it.label },
                secondLabels = SecondNs.Enumish.entries.map { it.label },
                firstResolvesOwn = FirstNs.Enumish.valueOf("Same") === FirstNs.Same,
                secondResolvesOwn = SecondNs.Enumish.valueOf("Same") === SecondNs.Same,
                firstListsForeign = FirstNs.Enumish.entries.any { it === secondSame },
                secondListsForeign = SecondNs.Enumish.entries.any { it === firstSame },
            )
        assertEquals(expected, actual)
    }
}
