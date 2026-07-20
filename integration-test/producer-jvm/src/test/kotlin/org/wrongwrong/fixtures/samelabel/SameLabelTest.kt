package org.wrongwrong.fixtures.samelabel

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

// 2 階層間の label 分離（docs/テストケース管理.md TC-BOX-074）。
// 同一パッケージ・同名末端でも LABEL_CLASH は階層内でのみ判定される（コンパイル成立が非発火の証明）
class SameLabelTest {
    // 各 valueOf は自階層の entries にのみ照合する
    @Test
    fun valueOfResolvesWithinOwnHierarchyOnly() {
        assertSame(FirstNs.Same, FirstNs.Enumish.valueOf("Same"))
        assertSame(SecondNs.Same, SecondNs.Enumish.valueOf("Same"))
    }

    // 他階層の末端 label は解決されない（各階層の entries は独立）
    @Test
    fun hierarchiesDoNotLeakIntoEachOther() {
        assertNull(FirstNs.Enumish.valueOfOrNull("NotInFirst"))
        assertSame(FirstNs.Same, FirstNs.Enumish.entries.single())
        assertSame(SecondNs.Same, SecondNs.Enumish.entries.single())
    }
}
