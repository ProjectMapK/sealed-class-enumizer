package org.wrongwrong.probe.alias

import org.wrongwrong.sealedClassEnumizer.Enumize as Ez

// docs/test/ケース04-診断.md DIA-67: import 別名表記の観測点。候補判定が未解決のアノテーション
// 型参照に触れて ICE となる（docs/test/保留.md GATE-02。Main からは参照しない）
@Ez
sealed interface AaIm {
    data object I1 : AaIm
}
