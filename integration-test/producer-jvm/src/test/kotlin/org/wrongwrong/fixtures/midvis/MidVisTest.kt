package org.wrongwrong.fixtures.midvis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// public 基底 + internal 中間 sealed + public 末端の box テスト（docs/テストケース管理.md TC-VIS-016）
class MidVisTest {
    // 中間 sealed には何も生成されず、その可視性（internal）は entries にも生成 API にも影響しない
    @Test
    fun internalIntermediateDoesNotAffectGeneration() {
        assertEquals(listOf("MA", "MB"), ViaMid.Enumish.entries.map { it.label })
    }

    // internal 中間経由の末端も通常どおり kind 解決される
    @Test
    fun leafViaInternalIntermediateResolvesNormally() {
        val value: ViaMid = MA
        assertSame(MA, value.asEnumish())
        assertEquals("MA", value.label)
    }
}
