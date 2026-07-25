package org.wrongwrong.fixtures.privatewide

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

// private 基底まわりのエッジ box テスト
// （docs/テストケース管理.md TC-VIS-061 = private 基底より広い末端 / TC-VIS-024 = private 基底内の internal companion）
class PrivateWideTest {
    // TC-VIS-061: private 基底より広い public 末端。別ファイル（このテスト）からも
    // 値・kind API（asEnumish / asEnumish().label）は public 側の規則 1（具体型）で利用できる
    @Test
    fun publicLeafOfPrivateBaseProvidesValueApiOutsideFile() {
        val leaf = WideLeaf(1)
        val kind: WideLeaf.Companion = leaf.asEnumish()
        assertSame(WideLeaf.Companion, kind)
        assertEquals("WideLeaf", kind.label)
    }

    // TC-VIS-024: private 基底の中の末端 + internal companion は実効可視性 private 同士で規則 1。
    // 露出エラーもフォールバックも起きない（docs/コンパイラプラグイン設計01.md §5.4 末尾の明示例）
    @Test
    fun internalCompanionInsidePrivateBaseUsesRule1() {
        assertEquals(listOf("Leaf"), observePrivateRule1Labels())
        assertTrue(privateRule1KindIsConcrete())
    }
}
