package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-52: 頭別名（エイリアスが先に解決される別ファイル配置）→ 注入スキップ
@Enumize
sealed interface NmThSi : NmThAlias {
    data object L : NmThSi
}
