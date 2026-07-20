package org.wrongwrong.fixtures.placement

import kotlin.test.Test
import kotlin.test.assertEquals

// 末端の配置（トップレベル / 別クラスネスト）と entries 順序の box テスト
// （docs/テストケース管理.md TC-ORD-002 / TC-ORD-003）
class PlacementTest {
    // TC-ORD-002: 全末端がトップレベル・中間 sealed 無し → FQN 序数順 = 単純名昇順
    @Test
    fun topLevelLeavesAreOrderedBySimpleName() {
        assertEquals(listOf("Aaa", "Bbb", "Ccc"), Flat.Enumish.entries.map { it.label })
    }

    // TC-ORD-003: 全末端が同一の別クラス（Crate）にネスト → 共通接頭辞 p.Crate. のため単純名昇順
    @Test
    fun leavesNestedInUnrelatedClassAreOrderedBySimpleName() {
        assertEquals(listOf("CrAaa", "CrBbb"), Cage.Enumish.entries.map { it.label })
    }
}
