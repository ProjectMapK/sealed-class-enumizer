package org.wrongwrong.diag.mppc

// TC-DIAG-061: 継承者が別ソースセット（jvmMain） → 本体の sealed 制約エラー（プラグイン補足診断は無し）
data object MppCJvm : MppC
