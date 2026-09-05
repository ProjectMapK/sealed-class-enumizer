package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-55: 非 @Enumize 基底のネスト Enumish・別名のネスト宣言・
// Enumish 名のプロパティ / 関数は RESERVED_NAME_CLASH 非発火

// 非 @Enumize クラスのネスト Enumish
class NmRnc {
    class Enumish
}

// Enumish 名のプロパティ / 関数は分類子でない
@Enumize
sealed interface OkR {
    val Enumish: Int get() = 0

    fun Enumish(): Int = 1

    data object L : OkR
}

// 別名のネスト分類子（Enumish でない）
@Enumize
sealed interface OkRn {
    class EnumishLike

    data object L : OkRn
}
