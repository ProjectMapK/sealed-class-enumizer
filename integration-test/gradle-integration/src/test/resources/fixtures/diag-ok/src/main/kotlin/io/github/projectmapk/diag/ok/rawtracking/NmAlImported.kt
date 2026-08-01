package io.github.projectmapk.diag.ok.rawtracking

import io.github.projectmapk.diag.ok.rawtracking.NmAl as NmAlI

// docs/test/ケース04-診断.md DIA-31/32: import 別名表記の各末端形。
// import 別名は file 単位の解決文脈であり、本ファイルにまとめること自体がその表記の適用範囲となる

// 末端 object は companion 概念が無く自身が kind
object NmImBar : NmAlI

// 末端 class（companion 無し）→ 自動生成成立
class NmImNoc(val v: Int) : NmAlI

// 明示 companion の併用形 → 成立
class NmImFoo(val v: Int) : NmAlI {
    companion object
}
