package org.wrongwrong.diag.xambapp

import org.wrongwrong.diag.xamb.XambSi

// TC-DIAG-021: 別モジュールで複数の末端 interface を実装（プラグイン適用） → ENUMIZE_AMBIGUOUS_KIND（利用側で報告）
class Cross : XambSi.LeafA, XambSi.LeafB
