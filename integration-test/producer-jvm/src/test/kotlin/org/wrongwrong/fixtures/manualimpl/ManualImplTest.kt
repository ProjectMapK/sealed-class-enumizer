package org.wrongwrong.fixtures.manualimpl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// 手動実装の許容（docs/概要.md §8）: kind 以外の Enumish 実装（階層内 = 末端 class 自身による実装）は
// entries / valueOf に現れず、sealed の継承者一覧には載る（kind 単位の網羅 when にその枝が必要になる）
class ManualImplTest {
    // entries は kind のみ（ManualLeaf の kind はその companion。手動実装の値は含まれない）
    @Test
    fun entriesContainOnlyKinds() {
        assertEquals(listOf("ManualLeaf", "Real"), WithManual.Enumish.entries.map { it.label })
    }

    // 手動実装の値が名乗る label は valueOf で解決されない
    @Test
    fun manualImplementationIsNotResolvedByValueOf() {
        assertNull(WithManual.Enumish.valueOfOrNull("manual-value"))
    }

    // 手動実装の値は Enumish として機能するが、kind（companion）とは別物
    @Test
    fun manualImplementationWorksAsEnumish() {
        val manual: WithManual.Enumish = ManualLeaf(1)
        assertEquals("manual-value", manual.label)
        assertSame(ManualLeaf.Companion, ManualLeaf(1).asEnumish())
    }

    // 手動実装は継承者一覧に載り、kind 単位の網羅 when にはその is 枝が必要になる（docs/概要.md §8）
    @Test
    fun kindWhenRequiresManualImplementationBranch() {
        val branches =
            WithManual.Enumish.entries.map { kind ->
                when (kind) {
                    WithManual.Real -> "real"
                    ManualLeaf.Companion -> "leaf-kind"
                    is ManualLeaf -> "manual"
                }
            }
        assertEquals(listOf("leaf-kind", "real"), branches)
    }
}
