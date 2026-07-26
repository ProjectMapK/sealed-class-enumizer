package org.wrongwrong.fixtures.order.mid

// トップレベル中間 × ネスト末端（docs/test/ケース03-順序.md ORD-03。
// 局所展開列 [NestA, NestB] が全末端 FQN 順とも一致する側）
sealed interface MidNest : MidRoot {
    data object NestA : MidNest

    data object NestB : MidNest
}
