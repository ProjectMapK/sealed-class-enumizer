package org.wrongwrong.fixtures.sweepprivateouter

import kotlin.test.Test
import kotlin.test.assertEquals

// private 外側クラスにネストした別ファイル末端の実挙動（docs/テストケース管理.md TC-LEAF-090）。
// 名指し不可の Leaf がトップレベル IR-only アクセサ経由で entries に載ることを、
// entries / valueOf の実行時挙動ごと固定する（docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3）
class SweepPrivateOuterTest {
    // 名前参照できない kind（HiddenHost.Leaf）も IR-only アクセサ経由で entries に載り、label / valueOf で観測できる
    @Test
    fun hiddenOuterLeafIsServedThroughEntries() {
        assertEquals(listOf("Leaf", "Visible"), PrivateOuterRoot.Enumish.entries.map { it.label })
        assertEquals("Leaf", PrivateOuterRoot.Enumish.valueOf("Leaf").label)
    }
}
