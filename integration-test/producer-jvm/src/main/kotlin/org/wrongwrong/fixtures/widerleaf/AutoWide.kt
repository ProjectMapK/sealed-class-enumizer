package org.wrongwrong.fixtures.widerleaf

// 基底（internal）より広い public 末端・companion 明示なし（TC-VIS-030）。
// 自動生成 companion は宣言 public・実効 public = 末端 → 規則 1（具体型）で規則 3 は発火しない
class AutoWide(val v: Int) : AutoBase
