package org.wrongwrong.fixtures.multienum

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 1 階層内の複数 enum 末端の box テスト
// （docs/テストケース管理.md TC-BOX-084 / TC-LEAF-032）
class MultiEnumTest {
    // TC-BOX-084: 各 enum が全体で 1 kind。定数（X/Y/P/Q）には展開されない
    @Test
    fun eachEnumLeafIsExactlyOneKind() {
        assertEquals(listOf("Alpha", "Beta", "Gamma"), Multi.Enumish.entries.map { it.label })
        assertSame(Multi.Alpha.Companion, Multi.Enumish.valueOf("Alpha"))
    }

    // TC-BOX-084: kind label は所属 enum 名で、enum の name は併存する
    @Test
    fun kindLabelIsEnumClassNameAndNameCoexists() {
        assertEquals(
            listOf("Alpha", "Beta", "X", "P"),
            listOf(Multi.Alpha.X.label, Multi.Beta.P.label, Multi.Alpha.X.name, Multi.Beta.P.name),
        )
    }

    // TC-LEAF-032: 定数の toString override は値側の表示にのみ影響し、kind の label / toString には影響しない
    @Test
    fun constantToStringOverrideDoesNotAffectKind() {
        assertEquals("x-custom", Multi.Gamma.X.toString())
        assertEquals(listOf("Gamma", "Gamma"), listOf(Multi.Gamma.X.label, Multi.Gamma.X.asEnumish().toString()))
    }
}
