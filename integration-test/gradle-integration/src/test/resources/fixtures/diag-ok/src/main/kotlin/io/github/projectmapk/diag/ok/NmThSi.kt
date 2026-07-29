package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-52: 頭別名（エイリアスが先に解決される別ファイル配置）→ 注入スキップ
@Enumize
sealed interface NmThSi : NmThAlias {
    data object L : NmThSi
}
