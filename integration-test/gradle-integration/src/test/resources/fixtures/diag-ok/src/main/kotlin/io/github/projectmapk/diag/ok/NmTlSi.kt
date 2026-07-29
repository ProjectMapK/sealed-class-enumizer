package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-52: 末端 Enumish 冗長宣言の別名形（別ファイル別名）→ 注入スキップ
@Enumize
sealed interface NmTlSi {
    data object L : NmTlSi, NmTlAlias
}
