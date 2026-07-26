package org.wrongwrong.fixtures.vis.narrow

// 基底（internal）より広い public 末端・companion 明示なし（docs/test/ケース02-可視性.md VIS-11:
// 自動生成 companion は宣言 public・実効可視性が末端へ追随し常に規則 1。露出診断も規則 3 も発火しない）
class AutoWide(val v: Int) : NarrowBase
