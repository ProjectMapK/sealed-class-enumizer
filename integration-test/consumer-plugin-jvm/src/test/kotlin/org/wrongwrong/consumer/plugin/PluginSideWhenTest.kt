package org.wrongwrong.consumer.plugin

import org.wrongwrong.fixtures.SI
import org.wrongwrong.fixtures.nested.Aaa
import org.wrongwrong.fixtures.nested.Bbb
import org.wrongwrong.fixtures.nested.NestedRoot
import kotlin.test.Test
import kotlin.test.assertEquals

// プラグインが両側に載る状態での跨モジュール kind-when（docs/テストケース管理.md TC-XM-008 / TC-XM-009）。
// いずれの when も else 無しでコンパイルできること自体が、生成 Enumish の inheritors 直列化と
// 各枝形の網羅性算入（V1-a/c の跨モジュール成立）の検査である
class PluginSideWhenTest {
    // TC-XM-008 / TC-XM-009 (a): companion 等値枝 + object 等値枝
    @Test
    fun companionAndObjectEqualityBranchesAreExhaustive() {
        val values: List<SI> = listOf(SI.Foo(1), SI.Bar)
        val branches = values.map { si ->
            when (si.asEnumish()) {
                SI.Foo.Companion -> "foo"
                SI.Bar -> "bar"
            }
        }
        assertEquals(listOf("foo", "bar"), branches)
    }

    // TC-XM-009 (b): companion 短縮形（SI.Foo ->）も跨モジュールの網羅判定に算入される（V1-c）
    @Test
    fun shorthandCompanionBranchIsExhaustive() {
        val branches = SI.Enumish.entries.map { kind ->
            when (kind) {
                SI.Foo -> "foo"
                SI.Bar -> "bar"
            }
        }
        assertEquals(listOf("bar", "foo"), branches)
    }

    // TC-XM-009 (c): is 枝（companion 型・object 型）も跨モジュールの網羅判定に算入される
    @Test
    fun isBranchesAreExhaustive() {
        val branches = SI.Enumish.entries.map { kind ->
            when (kind) {
                is SI.Foo.Companion -> "foo"
                is SI.Bar -> "bar"
            }
        }
        assertEquals(listOf("bar", "foo"), branches)
    }

    // TC-XM-009: 中間 sealed を持つ producer 別階層でも跨モジュール kind-when は else 不要
    @Test
    fun intermediateSealedHierarchyKindWhenIsExhaustive() {
        val values: List<NestedRoot> = listOf(Aaa, Bbb)
        val branches = values.map { value ->
            when (value.asEnumish()) {
                Aaa -> "aaa"
                Bbb -> "bbb"
            }
        }
        assertEquals(listOf("aaa", "bbb"), branches)
    }
}
