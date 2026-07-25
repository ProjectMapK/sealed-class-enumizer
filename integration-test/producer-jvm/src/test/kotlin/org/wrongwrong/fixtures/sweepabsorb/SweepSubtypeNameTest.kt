package org.wrongwrong.fixtures.sweepabsorb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 階層外サブタイプの名前は label 衝突対象外（docs/テストケース管理.md TC-MAN-079）。
// フィクスチャのコンパイル成立が LABEL_CLASH 非発火の実証で、吸収の意味論を実行時に観測する
class SweepSubtypeNameTest {
    // entries は末端の kind のみ（サブタイプ Twin では増えない）
    @Test
    fun entriesHoldOnlyLeafKinds() {
        assertEquals(listOf("Twin", "Wide"), SweepRoot.Enumish.entries.map { it.label })
    }

    // 同名サブタイプは Wide の kind に吸収され、label は "Wide"（末端 Twin とは別管轄）
    @Test
    fun sameNamedSubtypeIsAbsorbedIntoLeafKind() {
        val subtype: SweepRoot = Twin()
        assertSame(SweepRoot.Wide.Companion, subtype.asEnumish())
        assertEquals(
            listOf("Wide", "Twin"),
            listOf(subtype.asEnumish().label, SweepRoot.Twin.label),
        )
    }
}
