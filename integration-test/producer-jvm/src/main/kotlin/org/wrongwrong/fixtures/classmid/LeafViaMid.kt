package org.wrongwrong.fixtures.classmid

// 中間 sealed class 経由の末端・companion 明示なし（TC-LEAF-079）。
// raw-ref 再帰追跡が `:MidClass()` → MidClass → RootVia へ到達し companion が自動生成される
class LeafViaMid(val v: Int) : MidClass()
