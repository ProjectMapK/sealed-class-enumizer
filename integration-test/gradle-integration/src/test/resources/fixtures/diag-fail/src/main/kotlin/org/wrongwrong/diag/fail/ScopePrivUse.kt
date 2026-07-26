package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-28: private トップレベル基底を別ファイルから参照 → 言語可視性エラーのみ
fun scopePrivUse(): Int = ScopePriv.Enumish.entries.size
