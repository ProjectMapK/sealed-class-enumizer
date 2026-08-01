package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-28: private トップレベル基底を別ファイルから参照 → 言語可視性エラーのみ。
// 参照が基底と別ファイルであること自体が本ケースの成立条件である
fun scopePrivUse(): Int = ScopePriv.Enumish.entries.size
