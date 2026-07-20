package org.wrongwrong.fixtures

import org.wrongwrong.fixtures.nested.Aaa
import org.wrongwrong.fixtures.nested.Bbb
import org.wrongwrong.fixtures.nested.NestedRoot
import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals

// entries 順序の box テスト（docs/概要.md §5・docs/コンパイラプラグイン設計00.md §6）
class EntriesOrderTest {
    // TC-BOX-004: 全末端が基底にネストする構成では FQN 序数順 = 単純名昇順（宣言順ではない）
    @Test
    fun nestedLeavesAreOrderedByFqnNotDeclaration() {
        assertEquals(listOf("Aaa", "Mmm", "Zzz"), Ordered.Enumish.entries.map { it.label })
    }

    // TC-BOX-006 相当: 中間 sealed はその位置に継承者が入れ子展開され、entries には末端のみ載る。
    // NestedRoot の継承者は FQN 順で [Bbb, Mid] → Mid を展開して [Bbb, Aaa]（全体は FQN 順にならない）
    @Test
    fun intermediateSealedExpandsInPlace() {
        assertEquals(listOf("Bbb", "Aaa"), NestedRoot.Enumish.entries.map { it.label })
    }

    // 値経由でも中間 sealed の kind は現れない（Aaa は Mid ではなく自身の kind に対応する）
    @Test
    fun intermediateSealedHasNoKind() {
        val viaAaa: NestedRoot = Aaa
        val viaBbb: NestedRoot = Bbb
        assertEquals(listOf("Aaa", "Bbb"), listOf(viaAaa.label, viaBbb.label))
    }
}
