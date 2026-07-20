package org.wrongwrong.fixtures.baseimpl

import org.wrongwrong.fixtures.SI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

// 基底 Enumish（runtime-api）の手動実装は無制約であることの box テスト（docs/テストケース管理.md TC-BOX-079）
class FreeAgentTest {
    // 基底 Enumish は非 sealed の public interface のため自由に実装でき、コンパイルが通る（診断なし）
    @Test
    fun baseEnumishCanBeImplementedFreely() {
        assertEquals("FreeAgent", FreeAgent.label)
    }

    // FreeAgent はどの @Enumize 階層にも属さず、entries にも valueOf にも現れない
    @Test
    fun freeImplementationNeverAppearsInAnyEntries() {
        val freeAgent: Any = FreeAgent
        assertFalse(SI.Enumish.entries.any { it === freeAgent })
        assertNull(SI.Enumish.valueOfOrNull("FreeAgent"))
    }
}
