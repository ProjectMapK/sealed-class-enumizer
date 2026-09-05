package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-54: 既存ネスト宣言 Enumish → ENUMIZE_RESERVED_NAME_CLASH。
// class / object / interface の各亜種と、末端兼務でも予約名が優先されることを固定する

@Enumize
sealed interface Rn1 {
    class Enumish
}

@Enumize
sealed interface Rn2 {
    object Enumish
}

@Enumize
sealed interface Rn3 {
    interface Enumish
}

@Enumize
sealed interface Rn4 {
    object Enumish : Rn4
}
