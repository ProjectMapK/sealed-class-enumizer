package org.wrongwrong.fixtures.order.mid

// 多段中間（トップレベル中間 → ネスト中間 Deep。docs/test/ケース03-順序.md ORD-03）。
// Deep 配下はネスト末端 Bottom とトップレベル末端 Wide（深部 break）で構成する
sealed interface MidMulti : MidRoot {
    sealed interface Deep : MidMulti {
        data object Bottom : Deep
    }
}
