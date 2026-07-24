package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.EnumishCompanion
import kotlin.test.Test
import kotlin.test.assertEquals

// 複数階層の Companion を基底型で束ねる box テスト
// （docs/テストケース管理.md TC-BOX-040 / TC-MAN-010。EnumishCompanion<out T> の宣言側共変）
class CompanionBundleTest {
    // 射影なしで List<EnumishCompanion<Enumish>> に束ねられ、各 entries が正しく取れる
    @Test
    fun companionsBundleWithoutProjection() {
        val companions: List<EnumishCompanion<Enumish>> = listOf(SI.Enumish, Command.Enumish)
        val labels = companions.flatMap { companion -> companion.entries.map { it.label } }
        assertEquals(listOf("Bar", "Foo", "Builtin", "Custom"), labels)
    }
}
