package org.wrongwrong.sweep.taleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// 末端 object による生成 Enumish の冗長宣言を typealias 経由で書いた形。明示形（TC-DIAG-049 =
// data object L : SI, SI.Enumish）は注入スキップで許容される。その別名版の観測点
@Enumize
sealed interface SwTlSi {
    data object L : SwTlSi, SwTlAlias
}
