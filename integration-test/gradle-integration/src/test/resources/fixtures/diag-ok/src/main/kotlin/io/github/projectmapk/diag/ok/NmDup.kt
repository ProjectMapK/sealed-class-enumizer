package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-51: 型引数一致の生成 Enumish supertype 手動重複宣言 → 注入スキップ
@Enumize
sealed interface NmDup {
    data object L : NmDup, NmDup.Enumish
}
