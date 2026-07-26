package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// else 必須の位置依存 変種 2 用: public 基底 + 基底内ネスト private 末端
@Enumize
sealed interface ElsePub {
    private data object Hidden : ElsePub

    data object Shown : ElsePub
}
