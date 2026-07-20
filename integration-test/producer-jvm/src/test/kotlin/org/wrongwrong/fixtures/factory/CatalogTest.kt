package org.wrongwrong.fixtures.factory

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 名前つき companion の box テスト
// （docs/テストケース管理.md TC-LEAF-021 / TC-VIS-025 / TC-MAN-044 非発火 near-miss）
class CatalogTest {
    // TC-LEAF-021: kind = Made.Factory。label は末端宣言の単純名 "Made"（companion 宣言名に依存しない）。
    // 具体型（Made.Factory）で受けられること自体が規則 1 の検査
    @Test
    fun namedCompanionIsKindButLabelIsLeafName() {
        val kind: Catalog.Made.Factory = Catalog.Made(1).asEnumish()
        assertSame(Catalog.Made.Factory, kind)
        assertEquals("Made", kind.label)
        assertSame(Catalog.Made.Factory, Catalog.Enumish.valueOf("Made"))
    }

    // TC-VIS-025: internal な名前つき companion → 規則 2 フォールバック（返り値型は Catalog.Enumish）。
    // label は末端宣言名 "Forged" のまま（TC-MAN-044: LABEL_CLASH 非発火もこの階層の成立が証明）
    @Test
    fun internalNamedCompanionFallsBackToEnumishType() {
        val kind: Catalog.Enumish = Catalog.Forged(1).asEnumish()
        assertSame(Catalog.Forged.Factory, kind)
        assertEquals("Forged", kind.label)
    }

    // valueOf は companion 宣言名（Factory）では解決されない
    @Test
    fun valueOfDoesNotResolveCompanionName() {
        assertEquals(null, Catalog.Enumish.valueOfOrNull("Factory"))
    }
}
