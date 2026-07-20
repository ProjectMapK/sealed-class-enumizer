package org.wrongwrong.mpp.fixtures.tostring

import kotlin.test.Test
import kotlin.test.assertEquals

// kind の toString 2 原則が全バックエンドで成立することの box テスト
// （docs/テストケース管理.md TC-MPP-009・docs/概要.md §4・V11）
class ToStringMixTest {
    // 生成対象（Plain）は label を返し、明示実装（合成 = Auto・手動 = Manual/Styled・
    // 継承具象 = Inherited）はそれを尊重する。IR-only override（V11）が klib 直列化と
    // 各バックエンドの仮想ディスパッチを通ることの観測を兼ねる。
    // Plain の生成 companion は名指し（JVM）も末端型受け手の asEnumish()（klib）も NG のため
    // 基底型経由の asEnumish() で観測する（docs/修正方針案.md 反映待ち）
    @Test
    fun toStringFollowsTheTwoPrinciplesOnEveryTarget() {
        val plain: Show = Show.Plain(0)
        assertEquals(
            listOf("Plain", "Auto", "manual!", "styled!", "loud"),
            listOf(
                plain.asEnumish().toString(),
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
