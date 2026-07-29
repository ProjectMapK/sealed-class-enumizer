package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-28 用: private トップレベル基底（同一ファイル内では成立する側）
@Enumize
private sealed interface ScopePriv {
    data object L : ScopePriv
}
