package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// value class 末端の boxing 挙動が全ターゲットで整合することの box テスト
// （docs/テストケース管理.md TC-MPP-012）
class ValueClassBoxingTest {
    // インライン表現・boxed 表現のどちらから呼んでも kind は同一シングルトン
    // （value class 型の受け手からの直接呼び出しと基底型経由が同じ kind を返す）
    @Test
    fun asEnumishIsStableAcrossBoxing() {
        val boxed: Valued = Valued.Vc("b")
        assertSame(Valued.Vc("a").asEnumish(), boxed.asEnumish())
        assertSame(Valued.Vc.Companion, boxed.asEnumish())
    }

    // entries / label / enumizedClass も boxing 起因の差異なく一致する
    @Test
    fun labelAndEntriesAreUnaffectedByBoxing() {
        val boxed: Valued = Valued.Vc("c")
        assertEquals(
            listOf(listOf("None", "Vc"), listOf("Vc", "Vc")),
            listOf(
                Valued.Enumish.entries.map { it.label },
                listOf(boxed.label, boxed.asEnumish().enumizedClass.simpleName),
            ),
        )
    }
}
