package org.wrongwrong.fixtures.sweepprivateouter

import kotlin.test.Test
import kotlin.test.assertEquals

// private 外側クラスにネストした別ファイル末端の実挙動（docs/テストケース管理.md TC-LEAF-090）。
// doc の期待（ENUMIZE_KIND_NOT_ACCESSIBLE 発火）と異なりコンパイルが成立する現状を、
// entries / valueOf の実行時挙動ごと固定する（発火するよう修正されたら本フィクスチャごと要更新）
class SweepPrivateOuterTest {
    // 名前参照できない kind（HiddenHost.Leaf）も entries に載り、label / valueOf で観測できる
    @Test
    fun hiddenOuterLeafIsServedThroughEntries() {
        assertEquals(
            listOf("Leaf", "Visible"),
            PrivateOuterRoot.Enumish.entries.map { it.label },
        )
        assertEquals("Leaf", PrivateOuterRoot.Enumish.valueOf("Leaf").label)
    }
}
