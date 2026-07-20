package org.wrongwrong.fixtures.deepnested

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

// 多段中間 sealed の box テスト
// （docs/テストケース管理.md TC-LEAF-055 / TC-LEAF-056 / TC-ORD-016 / TC-BOX-008 / TC-ORD-021 / TC-BOX-067）
class DeepNestedTest {
    // TC-LEAF-055 / TC-ORD-016: entries は中間（DeepMid1 / DeepMid2）を含まず末端のみ。
    // 継承者 [DeepB, DeepMid2] の展開により entries = [DeepB, DeepA]（末端集合の FQN 順 [DeepA, DeepB] にならない）
    @Test
    fun multiStageIntermediatesAreFlattenedToLeaves() {
        assertEquals(listOf("DeepB", "DeepA"), Deep.Enumish.entries.map { it.label })
    }

    // TC-LEAF-056: 中間 sealed 型の変数へ asEnumish() を呼ぶと実体の末端の kind が返る（中間に kind は無い）
    @Test
    fun intermediateTypedValueResolvesToLeafKind() {
        val viaMid1: DeepMid1 = DeepA
        val viaMid2: DeepMid2 = DeepA
        assertSame(DeepA, viaMid1.asEnumish())
        assertSame(DeepA, viaMid2.asEnumish())
    }

    // TC-ORD-021 / TC-BOX-067 / TC-BOX-008: sealedSubclasses（直接継承者 = 中間を含む）と
    // entries（末端まで平坦化）は要素の集合も並びも一致しない
    @Test
    fun entriesDifferFromSealedSubclasses() {
        val direct = Deep::class.sealedSubclasses
        val flattened = Deep.Enumish.entries.map { it.enumizedClass }
        assertEquals(listOf(DeepMid1::class), direct)
        assertEquals(listOf(DeepB::class, DeepA::class), flattened)
        assertNotEquals(direct.toSet(), flattened.toSet())
    }
}
