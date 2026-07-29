package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-62: supertype の toString 抽象再宣言 + 手動実装なし
// → 言語 abstract 未実装エラーのみ・MC 不在（生成は充足に使えない）
object AtLeaf : AtAbs(), AtSi
