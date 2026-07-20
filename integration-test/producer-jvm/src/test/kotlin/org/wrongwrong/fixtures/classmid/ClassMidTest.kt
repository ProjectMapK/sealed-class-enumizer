package org.wrongwrong.fixtures.classmid

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 中間 sealed 経由の raw-ref 再帰追跡（V3）の box テスト
// （docs/テストケース管理.md TC-LEAF-023 = sealed interface 経由 / TC-LEAF-079 = sealed class 経由）
class ClassMidTest {
    // TC-LEAF-079: `:MidClass()` → MidClass → RootVia の再帰追跡で companion 自動生成が成立する
    @Test
    fun leafViaSealedClassMidGetsAutoCompanion() {
        assertSame(LeafViaMid.Companion, LeafViaMid(1).asEnumish())
        assertEquals("LeafViaMid", LeafViaMid(1).label)
    }

    // TC-LEAF-023: 中間 sealed interface 経由でも同様に自動生成が成立する
    @Test
    fun leafViaSealedInterfaceMidGetsAutoCompanion() {
        assertSame(LeafViaIface.Companion, LeafViaIface(1).asEnumish())
        assertEquals("LeafViaIface", LeafViaIface(1).label)
    }

    // 中間 sealed（class / interface とも）は entries に載らず、末端の kind のみが載る。
    // 継承者 [MidClass, MidIface]（FQN 順）を各位置で展開するため entries = [LeafViaMid, LeafViaIface]
    @Test
    fun intermediatesDoNotAppearInEntries() {
        assertEquals(listOf("LeafViaMid", "LeafViaIface"), RootVia.Enumish.entries.map { it.label })
    }
}
