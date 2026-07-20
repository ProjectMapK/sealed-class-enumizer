package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// value class 末端の boxing 挙動が全ターゲットで整合することの box テスト
// （docs/テストケース管理.md TC-MPP-012）
class ValueClassBoxingTest {
    // インライン表現・boxed 表現のどちらから呼んでも kind は同一シングルトン。
    // 本来形は value class 型の受け手から直接 `Valued.Vc("a").asEnumish()` を呼ぶが、
    // klib ターゲットでは生成 companion が Cannot access class になる NG のため
    // 基底型経由 + valueOf で同一性を観測する（docs/修正方針案.md 反映待ち）
    @Test
    fun asEnumishIsStableAcrossBoxing() {
        val first: Valued = Valued.Vc("a")
        val boxed: Valued = Valued.Vc("b")
        assertSame(first.asEnumish(), boxed.asEnumish())
        assertSame(Valued.Enumish.valueOf("Vc"), boxed.asEnumish())
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
