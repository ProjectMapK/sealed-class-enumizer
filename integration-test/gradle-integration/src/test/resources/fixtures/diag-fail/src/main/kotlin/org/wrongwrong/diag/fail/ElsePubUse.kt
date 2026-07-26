package org.wrongwrong.diag.fail

// else 必須の位置依存 変種 2: public 基底でも private ネスト kind が不可視の位置では else 必須
fun elsePubUse(si: ElsePub): String = when (si.asEnumish()) {
    ElsePub.Shown -> "shown"
}
