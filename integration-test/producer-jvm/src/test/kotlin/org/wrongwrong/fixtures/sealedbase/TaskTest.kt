package org.wrongwrong.fixtures.sealedbase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// sealed class 基底の box テスト（docs/テストケース管理.md TC-LEAF-076 / TC-LEAF-078）
class TaskTest {
    // TC-LEAF-076: 構築子呼び出し形 `:Task()` の supertype でも companion 自動生成（V3）が成立する
    @Test
    fun constructorCallSupertypeLeafGetsAutoCompanion() {
        assertSame(Task.Run.Companion, Task.Run(1).asEnumish())
        assertEquals(
            listOf("Run", Task.Run::class),
            listOf(Task.Run.Companion.label, Task.Run.Companion.enumizedClass),
        )
        assertEquals("Run", Task.Run.Companion.toString())
    }

    // TC-LEAF-078: sealed class 基底の object / data object 末端。kind = 自身
    @Test
    fun objectLeavesOfSealedClassBase() {
        assertSame(Task.Plain, Task.Plain.asEnumish())
        assertSame(Task.Done, Task.Done.asEnumish())
        assertEquals(listOf("Plain", "Done"), listOf(Task.Plain.label, Task.Done.label))
        // 非 data object は toString = label が生成され、data object は言語合成のまま
        assertEquals(listOf("Plain", "Done"), listOf(Task.Plain.toString(), Task.Done.toString()))
    }

    // entries は FQN 順（[Done, Plain, Run]）で kind を含む
    @Test
    fun entriesAreInFqnOrder() {
        assertEquals(listOf("Done", "Plain", "Run"), Task.Enumish.entries.map { it.label })
    }
}
