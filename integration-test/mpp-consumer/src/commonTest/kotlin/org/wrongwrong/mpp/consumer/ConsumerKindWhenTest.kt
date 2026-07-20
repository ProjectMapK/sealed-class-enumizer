package org.wrongwrong.mpp.consumer

import org.wrongwrong.mpp.fixtures.order.Box
import org.wrongwrong.mpp.fixtures.order.FqnOrder
import org.wrongwrong.mpp.fixtures.order.Mmm
import org.wrongwrong.mpp.fixtures.order.S
import org.wrongwrong.mpp.fixtures.order.Zzz
import org.wrongwrong.mpp.fixtures.order.aLower
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

// 跨モジュール網羅 when（V1-a の MPP 版。docs/テストケース管理.md TC-MPP-042/047）。
// else 無しでコンパイルが通ること自体が「生成 Enumish の inheritors が metadata / klib へ
// 直列化されている」ことの実証であり、各 platform のテスト実行がその klib 経路を確認する
class ConsumerKindWhenTest {
    // TC-MPP-042: 末端がすべて object の階層では else 無し kind-when が跨モジュールで成立する
    // （object の kind は source 宣言そのものなので生成 companion NG の影響を受けない）
    @Test
    fun objectLeafKindWhenNeedsNoElseAcrossModules() {
        val branches = FqnOrder.Enumish.entries.map { kind ->
            when (kind) {
                Box.Bbb -> "bbb"
                Mmm -> "mmm"
                S.Aaa -> "aaa"
                Zzz -> "zzz"
                aLower -> "alower"
            }
        }
        assertEquals(listOf("bbb", "mmm", "aaa", "zzz", "alower"), branches)
    }

    // 末端 class を含む階層の本来形（docs/概要.md §1）:
    //     when (kind) {
    //         SI.Foo.Companion -> "foo"
    //         SI.Bar -> "bar"
    //     }
    // は SI.Foo.Companion が Unresolved reference 'Companion'（JVM）となり書けない。
    // 同時に網羅性検査は「Add the 'Companion' branch or an 'else' branch」を報告する
    // （= inheritors の直列化自体は成立しており、companion の連結だけが落ちている）。
    // NG: 共通ソースの生成 companion が跨モジュールで未解決 — docs/修正方針案.md 反映待ち
    @Ignore
    @Test
    fun classLeafKindWhenNeedsNoElseAcrossModules() {
        fail("NG により無効化（本来形は上のコメントを参照）")
    }

    // enum 末端を含む階層の本来形（TC-MPP-044 の跨モジュール面）:
    //     when (kind) {
    //         Command.Builtin.Companion -> "builtin"
    //         Command.Custom.Companion -> "custom"
    //     }
    // も両 kind が生成 companion のため同 NG で書けない — docs/修正方針案.md 反映待ち
    @Ignore
    @Test
    fun enumLeafHierarchyKindWhenNeedsNoElse() {
        fail("NG により無効化（本来形は上のコメントを参照）")
    }
}
