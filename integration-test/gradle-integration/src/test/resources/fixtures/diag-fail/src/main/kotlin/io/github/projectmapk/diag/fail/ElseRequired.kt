package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md §11 の委譲対象: else 必須の位置依存 2 変種。
// 基底内ネストの private 末端はクラススコープであり、同一ファイルでも基底本体の外からは
// 名指しできないため、else 無し kind-when は網羅不成立（言語エラー）になる

// 変種 1: internal 基底 + 基底内ネスト private 末端
@Enumize
internal sealed interface ElseInt {
    private data class Hidden(val v: Int) : ElseInt

    data object A : ElseInt
}

internal fun elseIntUse(si: ElseInt): String = when (si.asEnumish()) {
    ElseInt.A -> "a"
}

// 変種 2: public 基底でも private ネスト kind が不可視の位置では else 必須
@Enumize
sealed interface ElsePub {
    private data object Hidden : ElsePub

    data object Shown : ElsePub
}

fun elsePubUse(si: ElsePub): String = when (si.asEnumish()) {
    ElsePub.Shown -> "shown"
}
