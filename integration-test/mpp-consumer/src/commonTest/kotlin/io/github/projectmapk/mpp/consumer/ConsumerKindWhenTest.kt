package io.github.projectmapk.mpp.consumer

import io.github.projectmapk.mpp.fixtures.Command
import io.github.projectmapk.mpp.fixtures.SI
import io.github.projectmapk.mpp.fixtures.order.Box
import io.github.projectmapk.mpp.fixtures.order.FqnOrder
import io.github.projectmapk.mpp.fixtures.order.Mmm
import io.github.projectmapk.mpp.fixtures.order.S
import io.github.projectmapk.mpp.fixtures.order.Zzz
import io.github.projectmapk.mpp.fixtures.order.aLower
import kotlin.test.Test
import kotlin.test.assertEquals

// 跨モジュール網羅 when（V1-a の MPP 版。docs/test/ケース05-境界横断.md XMP-38）。
// else 無しでコンパイルが通ること自体が「生成 Enumish の inheritors が metadata / klib へ
// 直列化されている」ことの実証であり、各 platform のテスト実行がその klib 経路を確認する
class ConsumerKindWhenTest {
    // XMP-38: 末端がすべて object の階層では else 無し kind-when が跨モジュールで成立する
    // （object の kind は source 宣言そのもの = 生成 companion を介さない経路）
    @Test
    fun objectLeafKindWhenNeedsNoElse() {
        val branches =
            FqnOrder.Enumish.entries.map { kind ->
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

    // XMP-38: 末端 class を含む階層では kind 枝を生成 companion の名指しで書く（docs/概要.md §1）。
    // 共通ソース由来の生成 companion が跨モジュールで解決できることの実証でもある
    @Test
    fun classLeafKindWhenNeedsNoElse() {
        val branches =
            SI.Enumish.entries.map { kind ->
                when (kind) {
                    SI.Foo.Companion -> "foo"
                    SI.Bar -> "bar"
                }
            }
        assertEquals(listOf("bar", "foo"), branches)
    }

    // XMP-38: enum 末端・data class 末端の kind はいずれも生成 companion
    @Test
    fun enumLeafHierarchyKindWhenNeedsNoElse() {
        val branches =
            Command.Enumish.entries.map { kind ->
                when (kind) {
                    Command.Builtin.Companion -> "builtin"
                    Command.Custom.Companion -> "custom"
                }
            }
        assertEquals(listOf("builtin", "custom"), branches)
    }
}
