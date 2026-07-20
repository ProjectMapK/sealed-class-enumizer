package org.wrongwrong.fixtures.internalleaves

// internal class 末端・companion 明示なし（TC-VIS-026）。
// 自動生成 companion は宣言 public・実効可視性は末端に一致（internal）→ 常に規則 1（具体型）
internal class AutoLeaf(val v: Int) : InternalLeaves
