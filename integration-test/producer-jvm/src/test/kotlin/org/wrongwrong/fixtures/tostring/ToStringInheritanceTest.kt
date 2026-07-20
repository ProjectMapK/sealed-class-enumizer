package org.wrongwrong.fixtures.tostring

import kotlin.test.Test
import kotlin.test.assertEquals

// toString の 2 原則のうち継承経路の枝の box テスト
// （docs/テストケース管理.md TC-LEAF-060 / TC-BOX-045 / TC-BOX-046 / TC-BOX-047）
class ToStringInheritanceTest {
    // TC-LEAF-060 / TC-BOX-045: 継承経路上に Any 以外の具象 toString を持つ kind には生成しない
    // （companion / 末端 object のどちらでも継承実装が尊重される）
    @Test
    fun inheritedConcreteToStringIsRespected() {
        assertEquals(
            listOf("base-display", "base-display"),
            listOf(Displayed.ViaBase.Companion.toString(), Displayed.ObjViaBase.toString()),
        )
    }

    // TC-BOX-046: final 継承 toString でも生成スキップにより override 衝突は起きず、親の実装が使われる
    @Test
    fun finalInheritedToStringDoesNotClash() {
        assertEquals("fixed-display", Displayed.ViaFixed.Companion.toString())
    }

    // TC-BOX-047: supertype の抽象再宣言は kind 側の手動実装で充足され、その手動値が返る
    @Test
    fun abstractRedeclarationIsSatisfiedByManualImplementation() {
        assertEquals("answered", Displayed.ViaDemand.Companion.toString())
    }

    // toString を生成しない場合でも label は通常どおり生成される
    @Test
    fun labelIsGeneratedRegardlessOfToStringBranch() {
        assertEquals(
            listOf("ViaBase", "ObjViaBase", "ViaFixed", "ViaDemand"),
            listOf(
                Displayed.ViaBase.Companion.label,
                Displayed.ObjViaBase.label,
                Displayed.ViaFixed.Companion.label,
                Displayed.ViaDemand.Companion.label,
            ),
        )
    }
}
