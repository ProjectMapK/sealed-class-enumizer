package org.wrongwrong.fixtures.midorder

// 兄弟の中間位置に立つ中間 sealed。末端 P / Q は内側にネスト（TC-ORD-014）
sealed interface Mid14 : X14 {
    data object P : Mid14

    data object Q : Mid14
}
