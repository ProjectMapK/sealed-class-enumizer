package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-55: 別名のネスト分類子（Enumish でない）→ 非発火
@Enumize
sealed interface OkRn {
    class EnumishLike

    data object L : OkRn
}
