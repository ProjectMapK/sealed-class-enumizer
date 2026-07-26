package org.wrongwrong.probe.finalm

// docs/test/ケース04-診断.md DIA-70: 階層外クラスからの final 具象（label）継承末端。
// 生成 override が言語衝突するか生成スキップされるかを固定する
object FmLeaf : FmBase(), FmSi
