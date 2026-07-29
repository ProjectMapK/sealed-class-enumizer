package io.github.projectmapk.diag.mppf

// docs/test/ケース04-診断.md DIA-10: 継承者が別ソースセット（jvmMain）
// → 言語の sealed 制約エラーのみ（プラグイン補足診断なし）
data object MppCJvm : MppC
