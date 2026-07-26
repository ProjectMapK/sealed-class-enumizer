package org.wrongwrong.fixtures.manual.tostr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// toString 2 原則の全分岐と label シャドーイングの box テスト
// （docs/test/ケース01-生成と実行時API.md API-33〜API-35・API-37〜API-39）
class ToStringTest {
    // docs/test/ケース01-生成と実行時API.md API-33: 明示実装の無い kind（Tagged の自動生成 companion）へは
    // toString=label が必ず生成される（si / plain 側の観測はケース01 の各正典が担う）
    @Test
    fun kindsWithoutExplicitToStringGetLabel() {
        assertEquals("Tagged", Displayed.Tagged.Companion.toString())
    }

    // docs/test/ケース01-生成と実行時API.md API-34: kind 自身の手動 toString
    // （data object 末端 Manual・companion 手動 Styled）を尊重して生成しない
    @Test
    fun manualToStringIsRespected() {
        assertEquals(
            listOf("custom", "companion-custom"),
            listOf(Displayed.Manual.toString(), Displayed.Styled.Companion.toString()),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-35: 継承経路の Any 以外の具象 toString で非生成
    // （companion 継承 ViaBase・object 自身継承 ObjViaBase）・final 継承 ViaFixed も衝突なしスキップ
    @Test
    fun inheritedConcreteToStringSkipsGeneration() {
        assertEquals(
            listOf("base-display", "base-display", "fixed-display"),
            listOf(
                Displayed.ViaBase.Companion.toString(),
                Displayed.ObjViaBase.toString(),
                Displayed.ViaFixed.Companion.toString(),
            ),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-37: supertype の toString 抽象再宣言は kind 側の
    // 手動実装で充足される（生成は充足に使えない。未実装時の言語エラーはケース04 が正典）
    @Test
    fun abstractRedeclarationSatisfiedManually() {
        assertEquals("answered", Displayed.ViaDemand.Companion.toString())
    }

    // docs/test/ケース01-生成と実行時API.md API-38: label は toString 分岐と無関係に常に生成される
    @Test
    fun labelIsAlwaysGenerated() {
        assertEquals(
            listOf("Manual", "ObjViaBase", "Styled", "Tagged", "ViaBase", "ViaDemand", "ViaFixed"),
            Displayed.Enumish.entries.map { it.label },
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-39: メンバー label が拡張 label をシャドーし、
    // entries / valueOf は kind label 基準のまま・確実な取得経路は asEnumish().label
    // （ENUMIZE_EXTENSION_SHADOWED 警告の発火照合はケース04 が正典）
    @Test
    fun memberLabelShadowsExtension() {
        val tagged = Displayed.Tagged(label = "user-value")
        assertEquals(listOf("user-value", "Tagged"), listOf(tagged.label, tagged.asEnumish().label))
        assertNull(Displayed.Enumish.valueOfOrNull("user-value"))
        assertSame(Displayed.Tagged.Companion, Displayed.Enumish.valueOf("Tagged"))
    }
}
