package org.wrongwrong.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 値単位 / kind 単位の when の枝形 box テスト
// （docs/テストケース管理.md TC-BOX-027 / TC-BOX-028 / TC-BOX-060）
class SIWhenBranchTest {
    // TC-BOX-027: 値単位 when はスマートキャストが効き else 不要（sealed の地力）
    @Test
    fun valueWhenSmartCasts() {
        val si: SI = SI.Foo(41)
        val result =
            when (si) {
                is SI.Foo -> si.v + 1
                SI.Bar -> 0
            }
        assertEquals(42, result)
    }

    // TC-BOX-028: kind 単位 when では値の型は絞り込まれない（si の静的型は SI のまま）。
    // 実行時は値の kind に対応する枝が選ばれる
    @Test
    fun kindWhenDoesNotNarrowValueType() {
        val si: SI = SI.Foo(1)
        val result =
            when (si.asEnumish()) {
                SI.Foo.Companion -> {
                    // 分岐内でも si は SI のまま（member v へは is 判定が必要なことを実行時に確認）
                    assertTrue(si is SI.Foo)
                    "foo"
                }
                SI.Bar -> "bar"
            }
        assertEquals("foo", result)
    }

    // TC-BOX-060: companion 等値枝・object 等値枝・is 枝のいずれも実行時に正しく分岐する。
    // 短縮形（SI.Foo ->）は companion への等値枝として書け、網羅性にも算入される（V1-c の実測）
    @Test
    fun kindWhenBranchShapes() {
        fun byEquality(si: SI): String =
            when (si.asEnumish()) {
                SI.Foo.Companion -> "foo"
                SI.Bar -> "bar"
            }

        fun byShortForm(si: SI): String =
            when (si.asEnumish()) {
                SI.Foo -> "foo"
                SI.Bar -> "bar"
            }

        fun byIsBranch(si: SI): String =
            when (si.asEnumish()) {
                is SI.Foo.Companion -> "foo"
                is SI.Bar -> "bar"
            }

        val values = listOf<SI>(SI.Foo(1), SI.Bar)
        assertEquals(listOf("foo", "bar"), values.map(::byEquality))
        assertEquals(listOf("foo", "bar"), values.map(::byShortForm))
        assertEquals(listOf("foo", "bar"), values.map(::byIsBranch))
    }
}
