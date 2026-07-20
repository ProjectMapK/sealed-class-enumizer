package org.wrongwrong.mpp.fixtures

import org.wrongwrong.mpp.fixtures.wider.Wide
import org.wrongwrong.mpp.fixtures.wider.WideBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 可視性混在（規則 1 / 規則 2）の判定が全 platform で一致することの box テスト
// （docs/テストケース管理.md TC-GAP-017 の正値側・エッジ §1.3）。
// 規則 3（ENUMIZE_KIND_TYPE_NOT_DENOTABLE）の発火側はコンパイル失敗を要するため TestKit 管轄
class VisibilityRulesTest {
    // 規則 1: companion 可視性 >= 末端 → asEnumish の返り値型は companion 型
    // （Wide.Companion への代入がコンパイルできること自体が返り値型の実証）
    @Test
    fun rule1UsesCompanionTypeAsReturnType() {
        val kind: Wide.Companion = Wide().asEnumish()
        assertSame(Wide.Companion, kind)
        assertEquals("Wide", kind.label)
    }

    // internal 基底の階層 API は同一モジュールからは通常どおり使える（エッジ §1）
    @Test
    fun internalBaseHierarchyApiWorksInsideModule() {
        assertEquals(listOf("Wide"), WideBase.Enumish.entries.map { it.label })
        assertSame(Wide.Companion, WideBase.Enumish.valueOf("Wide"))
    }

    // 規則 2: internal companion → 返り値型は生成 Enumish へフォールバック
    // （Mixed.Enumish への代入と、実体が companion シングルトンであることを観測）
    @Test
    fun rule2FallsBackToGeneratedEnumishType() {
        val kind: Mixed.Enumish = Mixed.Half(1).asEnumish()
        assertSame(Mixed.Half.Companion, kind)
        assertEquals(listOf("Full", "Half"), Mixed.Enumish.entries.map { it.label })
    }
}
