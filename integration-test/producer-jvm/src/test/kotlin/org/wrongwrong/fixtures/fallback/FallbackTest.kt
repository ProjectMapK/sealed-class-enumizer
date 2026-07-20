package org.wrongwrong.fixtures.fallback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 各末端種別 × internal companion × 規則 2 フォールバックの box テスト
// （docs/テストケース管理.md TC-GAP-012 / TC-GAP-013。
//  TC-GAP-010 / TC-GAP-011 は interface メンバーに internal を付けられない言語制約により構成不能 —
//  FallbackHost.kt の NG コメント参照。ここでは public companion の対照として kind 解決のみ検証する）
class FallbackTest {
    // TC-GAP-010 対照: interface 末端（public companion）の default asEnumish が companion を返す
    @Test
    fun interfaceLeafResolvesKindViaDefaultImplementation() {
        val kind: FallbackHost.Enumish = CustomImpl().asEnumish()
        assertSame(FallbackHost.Custom.Companion, kind)
        assertEquals("Custom", kind.label)
    }

    // TC-GAP-011 対照: fun interface 末端でも SAM の抽象は run() の 1 つに保たれ（ラムダ構成が成立）、
    // default asEnumish と両立する
    @Test
    fun funInterfaceLeafKeepsSam() {
        val fn: FallbackHost = FallbackHost.Fn { it + 1 }
        val kind: FallbackHost.Enumish = fn.asEnumish()
        assertSame(FallbackHost.Fn.Companion, kind)
        assertEquals(2, (fn as FallbackHost.Fn).run(1))
    }

    // TC-GAP-012: value class 末端 × internal companion × 規則 2。boxing 後も kind は companion シングルトン
    @Test
    fun valueClassLeafKeepsIdentityAcrossBoxingWithFallback() {
        val boxed: FallbackHost = FallbackHost.Val(1)
        val kind: FallbackHost.Enumish = FallbackHost.Val(2).asEnumish()
        assertSame(kind, boxed.asEnumish())
        assertSame(FallbackHost.Val.Companion, boxed.asEnumish())
    }

    // TC-GAP-013: enum 末端（V4）× internal companion × 規則 2。name / label の管轄分離と kind toString の 2 原則
    @Test
    fun enumLeafWithInternalCompanionFallsBack() {
        val kind: FallbackHost.Enumish = FallbackHost.Cmd.HELP.asEnumish()
        assertSame(FallbackHost.Cmd.Companion, kind)
        assertEquals(listOf("HELP", "Cmd", "Cmd"), listOf(FallbackHost.Cmd.HELP.name, kind.label, kind.toString()))
    }

    // 4 種別すべての kind が entries に載る
    @Test
    fun entriesContainAllFallbackKinds() {
        assertEquals(listOf("Cmd", "Custom", "Fn", "Val"), FallbackHost.Enumish.entries.map { it.label })
    }
}
