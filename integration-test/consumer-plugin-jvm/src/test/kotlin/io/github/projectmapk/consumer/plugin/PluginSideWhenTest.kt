package io.github.projectmapk.consumer.plugin

import io.github.projectmapk.fixtures.mid.LeafViaIface
import io.github.projectmapk.fixtures.mid.LeafViaMid
import io.github.projectmapk.fixtures.mid.MidClass
import io.github.projectmapk.fixtures.mid.RootVia
import io.github.projectmapk.fixtures.si.SI
import kotlin.test.Test
import kotlin.test.assertEquals

// プラグインが両側に載る状態での跨 module kind-when（docs/test/ケース05-境界横断.md XMP-10）。
// いずれの when も else 無しでコンパイルできること自体が、生成 Enumish の inheritors 直列化と
// 各枝形の網羅性算入（V1-a/c の跨 module 成立）の検査である
class PluginSideWhenTest {
    // docs/test/ケース05-境界横断.md XMP-10: companion 等値枝 + object 等値枝
    @Test
    fun companionAndObjectEqualityBranches() {
        val values: List<SI> = listOf(SI.Foo(1), SI.Bar)
        val branches = values.map { si ->
            when (si.asEnumish()) {
                SI.Foo.Companion -> "foo"
                SI.Bar -> "bar"
            }
        }
        assertEquals(listOf("foo", "bar"), branches)
    }

    // docs/test/ケース05-境界横断.md XMP-10: companion 短縮形（SI.Foo ->）も網羅判定に算入される（V1-c）
    @Test
    fun shorthandCompanionBranch() {
        val branches =
            SI.Enumish.entries.map { kind ->
                when (kind) {
                    SI.Foo -> "foo"
                    SI.Bar -> "bar"
                }
            }
        assertEquals(listOf("bar", "foo"), branches)
    }

    // docs/test/ケース05-境界横断.md XMP-10: is 枝（companion 型・object 型）も網羅判定に算入される
    @Test
    fun isBranches() {
        val branches =
            SI.Enumish.entries.map { kind ->
                when (kind) {
                    is SI.Foo.Companion -> "foo"
                    is SI.Bar -> "bar"
                }
            }
        assertEquals(listOf("bar", "foo"), branches)
    }

    // docs/test/ケース05-境界横断.md XMP-10: 中間 sealed（MidClass / MidIface）を持つ producer 階層でも
    // 跨 module kind-when は else 不要。中間 companion 末端（MidClass.Companion）は object 等値枝で受ける
    // （期待順は DFS in-place 展開 [LeafViaMid, Companion, LeafViaIface]。
    // docs/test/ケース01-生成と実行時API.md API-30）
    @Test
    fun intermediateHierarchyKindWhen() {
        val branches =
            RootVia.Enumish.entries.map { kind ->
                when (kind) {
                    LeafViaIface.Companion -> "via-iface"
                    LeafViaMid.Companion -> "via-mid"
                    MidClass.Companion -> "companion-leaf"
                }
            }
        assertEquals(listOf("via-mid", "companion-leaf", "via-iface"), branches)
    }
}
