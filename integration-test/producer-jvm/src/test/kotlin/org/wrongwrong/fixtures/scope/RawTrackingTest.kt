package org.wrongwrong.fixtures.scope

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.wrongwrong.fixtures.scope.other.Base as OtherBase
import org.wrongwrong.fixtures.scope.target.Base as DecoyBase
import org.wrongwrong.fixtures.scope.target.Holder
import org.wrongwrong.fixtures.scope.target.ShellHost
import org.wrongwrong.fixtures.scope.target.ViaImport
import org.wrongwrong.fixtures.scope.target.ViaStar

// raw 追跡スコープ順の競合 3 形（docs/test/ケース01-生成と実行時API.md API-51）。
// 表記単独形の成立はケース04 DIA-31/32 が正典
class RawTrackingTest {
    // 競合 3 形の優先関係を entries 所属 / 非所属 + コンパイル成立で固定する
    @Test
    fun scopePriorityDecidesMembership() {
        // (1) 明示 import Holder.Base vs 同一 pkg 囮 = import 勝ち → 所属
        assertEquals(listOf("ViaImport"), Holder.Base.Enumish.entries.map { it.label })
        assertSame(Holder.Base.Enumish.valueOf("ViaImport"), ViaImport().asEnumish())

        // (2) scope.other.* star import vs 同一 pkg 囮 = 同一 pkg 勝ち → 非所属。
        // ViaStar は囮 interface の実装としてコンパイル成功している（sealed の同一 pkg 制約により
        // scope.other.Base が勝つ構成はコンパイル不能）
        assertEquals(emptyList(), OtherBase.Enumish.entries)
        val viaStar: Any = ViaStar()
        assertTrue(viaStar is DecoyBase)
        assertFalse(viaStar is Holder.Base)

        // (3) 明示 import vs ホストの内側ネスト同名 = ネスト勝ち → 非所属
        val viaNest: Any = ShellHost.ViaNest()
        assertTrue(viaNest is ShellHost.Base)
        assertFalse(viaNest is Holder.Base)
    }
}
