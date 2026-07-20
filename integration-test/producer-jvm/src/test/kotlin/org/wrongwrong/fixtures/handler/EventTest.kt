package org.wrongwrong.fixtures.handler

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// fun interface 末端（V10-c: SAM 保持）の box テスト
// （docs/テストケース管理.md TC-LEAF-010 / TC-LEAF-038 / TC-LEAF-081 / TC-BOX-068）
class EventTest {
    // TC-LEAF-010 / TC-LEAF-038: asEnumish の default 実装生成後も SAM の抽象は handle 1 つに保たれ、
    // ラムダで構成した値の kind は Handler.Companion になる
    @Test
    fun samLambdaOfExplicitCompanionLeafSharesKind() {
        val handler: Event = Event.Handler { it + 1 }
        assertSame(Event.Handler.Companion, handler.asEnumish())
        assertEquals("Handler", handler.label)
        assertEquals(42, (handler as Event.Handler).handle(41))
    }

    // TC-LEAF-081: companion 自動生成の fun interface 末端でも SAM 変換と kind 解決が両立する
    @Test
    fun samLambdaOfAutoCompanionLeafSharesKind() {
        val listener: Event = Event.Listener { it * 2 }
        assertSame(Event.Listener.Companion, listener.asEnumish())
        assertEquals(84, (listener as Event.Listener).onEvent(42))
    }

    // TC-BOX-068: entries には fun interface の kind が 1 つずつ載り、enumizedClass は末端を返す
    @Test
    fun entriesAndEnumizedClassForFunInterfaceLeaves() {
        assertEquals(listOf("Handler", "Listener"), Event.Enumish.entries.map { it.label })
        assertEquals(Event.Handler::class, Event.Handler.Companion.enumizedClass)
    }
}
