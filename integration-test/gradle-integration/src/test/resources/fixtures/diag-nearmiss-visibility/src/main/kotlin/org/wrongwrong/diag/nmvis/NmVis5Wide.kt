package org.wrongwrong.diag.nmvis

// TC-DIAG-092: 基底より広い末端 object → 非発火（kind は自身を返すためdocs/コンパイラプラグイン設計01.md §5.4 規則の対象外）
object NmVis5Wide : NmVis5
