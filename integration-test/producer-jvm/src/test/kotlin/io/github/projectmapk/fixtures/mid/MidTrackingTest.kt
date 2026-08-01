package io.github.projectmapk.fixtures.mid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

// 中間 sealed 経由の自動生成・中間非生成・中間 companion の両形の box テスト
// （docs/test/ケース01-生成と実行時API.md §7）
class MidTrackingTest {
    // docs/test/ケース01-生成と実行時API.md API-29: 中間 sealed class / sealed interface 経由の
    // raw 追跡再帰で末端 companion が自動生成される
    @Test
    fun leavesViaIntermediatesGetAutoCompanions() {
        assertSame(LeafViaMid.Companion, LeafViaMid(1).asEnumish())
        assertSame(LeafViaIface.Companion, LeafViaIface(1).asEnumish())
        assertSame(LeafViaMid.Companion, RootVia.Enumish.valueOf("LeafViaMid"))
        assertSame(LeafViaIface.Companion, RootVia.Enumish.valueOf("LeafViaIface"))
    }

    // docs/test/ケース01-生成と実行時API.md API-30: 中間には何も生成されず entries 非掲載
    // （末端まで平坦化）。中間の明示 companion へも Enumish 非注入（kind 非成立）で、
    // 中間型変数の asEnumish は末端 kind へ実体解決される。
    // 探索の上端も同じで、非注釈 sealed 祖先の側（ViaOutside）は階層に数えない。
    // 期待順は直接継承者の FQN 順 [MidClass, MidClass.Companion, MidIface] を DFS で
    // in-place 展開した [LeafViaMid, Companion, LeafViaIface]（docs/test/ケース03-順序.md §1）
    @Test
    fun intermediatesHaveNoKind() {
        assertEquals(
            listOf("LeafViaMid", "Companion", "LeafViaIface"),
            RootVia.Enumish.entries.map { it.label },
        )
        // MidIface の明示 companion は階層を実装せず kind ではない
        val midCompanion: Any = MidIface.Companion
        assertFalse(midCompanion is RootVia.Enumish)
        // 祖先直下の非所属メンバーにも kind は成立しない（探索は基底から下だけを見る）
        val outside: Any = ViaOutside
        assertFalse(outside is RootVia.Enumish)
        // 中間型変数からの asEnumish は末端 kind を返す
        val viaIface: MidIface = LeafViaIface(1)
        assertSame(LeafViaIface.Companion, viaIface.asEnumish())
        val viaMid: MidClass = LeafViaMid(2)
        assertSame(LeafViaMid.Companion, viaMid.asEnumish())
    }

    // docs/test/ケース01-生成と実行時API.md API-52: 中間 sealed の companion（`: RootVia`）は
    // 末端として成立する（COMPANION_LEAF_CONFLICT は外側 = 末端のみ検査）。kind = 自身・label = 宣言名
    @Test
    fun intermediateCompanionLeafIsAllowed() {
        assertSame(MidClass.Companion, MidClass.Companion.asEnumish())
        assertEquals("Companion", MidClass.Companion.label)
        assertSame(MidClass.Companion, RootVia.Enumish.valueOf("Companion"))
    }
}
