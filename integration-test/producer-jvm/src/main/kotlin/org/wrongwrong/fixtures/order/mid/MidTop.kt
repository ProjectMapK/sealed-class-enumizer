package org.wrongwrong.fixtures.order.mid

// トップレベル中間 × トップレベル末端（docs/test/ケース03-順序.md ORD-03。
// 配下の Early / Late は展開位置に留まり、全末端 FQN 一括整列との break を作る）
sealed interface MidTop : MidRoot
