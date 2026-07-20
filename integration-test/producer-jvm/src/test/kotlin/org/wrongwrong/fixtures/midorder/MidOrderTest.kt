package org.wrongwrong.fixtures.midorder

import kotlin.test.Test
import kotlin.test.assertEquals

// 中間 sealed の入れ子展開と entries 順序の境界 box テスト
// （docs/テストケース管理.md TC-ORD-012 / 014 / 015 / 017 / 018 / 019 / 048 / 049 / 051 / 064）
class MidOrderTest {
    // TC-ORD-012: 末端が中間の内側にネスト → 継承者 [C, Mid] の展開が全体 FQN 序数順と一致
    @Test
    fun nestedIntermediateMatchesFqnOrder() {
        assertEquals(listOf("C", "A", "B"), R12.Enumish.entries.map { it.label })
    }

    // TC-ORD-017: 多段中間（全てネスト）でも接頭辞連鎖により FQN 序数順と一致
    @Test
    fun deepNestedIntermediatesMatchFqnOrder() {
        assertEquals(listOf("C", "A", "B"), R17.Enumish.entries.map { it.label })
    }

    // TC-ORD-014: 兄弟の中間位置での in-place 展開（[Aaa14, Mid14, Zzz14] → [Aaa14, P, Q, Zzz14]）
    @Test
    fun intermediateExpandsInPlaceBetweenSiblings() {
        assertEquals(listOf("Aaa14", "P", "Q", "Zzz14"), X14.Enumish.entries.map { it.label })
    }

    // TC-ORD-015: 中間位置の中間 sealed の継承者がトップレベル → 局所的に FQN 順が崩れる
    @Test
    fun topLevelLeafOfMiddleIntermediateBreaksFqnOrder() {
        assertEquals(listOf("Bbb15", "Aaa15", "Yyy15"), R15.Enumish.entries.map { it.label })
    }

    // TC-ORD-016 は deepnested 側で検証（DeepNestedTest）

    // TC-ORD-018: 中間が基底ネストでも末端がトップレベルなら break（末端の配置が効く）
    @Test
    fun leafPlacementNotIntermediatePlacementDrivesOrder() {
        assertEquals(listOf("Bbb18", "Aaa18"), R18.Enumish.entries.map { it.label })
    }

    // TC-ORD-019: 複数兄弟中間・ネスト / トップレベル混在（[D19, M1, M2] → [D19, A19, B19, C19]）
    @Test
    fun multipleSiblingIntermediatesExpandStably() {
        assertEquals(listOf("D19", "A19", "B19", "C19"), R19.Enumish.entries.map { it.label })
    }

    // TC-ORD-048: 継承者比較 "Sep2.M" vs "M0" は 'S' > 'M' で決着（entries = [M0, Zzz48]）
    @Test
    fun sep2OrderIsDecidedByFirstDifferingChar() {
        assertEquals(listOf("M0", "Zzz48"), Sep2.Enumish.entries.map { it.label })
    }

    // TC-ORD-049: FQN が接頭辞関係にある 2 末端は短い方（Foo）が先行する
    @Test
    fun prefixFqnSortsBeforeLongerName() {
        assertEquals(listOf("Foo", "FooBar"), Pfx.Enumish.entries.map { it.label })
    }

    // TC-ORD-051: 中間配下でネスト / トップレベル混在の部分 break（訂正メモ準拠: [Outside51, Inside, Top51]）
    @Test
    fun partialBreakInsideIntermediateInheritorList() {
        assertEquals(listOf("Outside51", "Inside", "Top51"), SI51.Enumish.entries.map { it.label })
    }

    // TC-ORD-064: entries = [Foo0, Bar]。ネスト末端 Bar の FQN が中間 Foo の FQN を接頭辞に持つ
    @Test
    fun separatorBoundaryHierarchyExpandsAsDocumented() {
        assertEquals(listOf("Foo0", "Bar"), Sep64.Enumish.entries.map { it.label })
    }
}
