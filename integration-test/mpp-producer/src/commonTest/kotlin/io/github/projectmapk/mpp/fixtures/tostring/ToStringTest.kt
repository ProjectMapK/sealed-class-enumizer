package io.github.projectmapk.mpp.fixtures.tostring

import kotlin.test.Test
import kotlin.test.assertEquals

// kind の toString 2 原則が全バックエンドで成立することの box テスト
// （docs/test/ケース05-境界横断.md XMP-35・docs/概要.md §4・V11）
class ToStringTest {
    // 生成対象（Plain）は label を返し、明示実装（合成 = Auto・手動 = Manual/Styled・
    // 継承具象 = Inherited）はそれを尊重する。IR-only override（V11）が klib 直列化と
    // 各バックエンドの仮想ディスパッチを通ることの観測を兼ねる
    @Test
    fun toStringFollowsTheTwoPrinciplesOnEveryTarget() {
        assertEquals(
            listOf("Plain", "Auto", "manual!", "styled!", "loud"),
            listOf(
                Show.Plain.Companion.toString(),
                Show.Auto.toString(),
                Show.Manual.toString(),
                Show.Styled.Companion.toString(),
                Show.Inherited.Companion.toString(),
            ),
        )
    }

    // 手動 toString の有無に関わらず label は通常どおり生成される
    @Test
    fun labelIsGeneratedRegardlessOfToString() {
        assertEquals(
            listOf("Auto", "Inherited", "Manual", "Plain", "Styled"),
            Show.Enumish.entries.map { it.label },
        )
    }

    // 仮想ディスパッチ（静的型 Enumish 経由の toString 呼び出し）でも同一の表示になる
    @Test
    fun toStringDispatchesVirtuallyThroughEnumishType() {
        assertEquals(
            listOf("Auto", "loud", "manual!", "Plain", "styled!"),
            Show.Enumish.entries.map { it.toString() },
        )
    }
}
