package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-15: @Enumize 付き中間は NESTED・その配下末端には MULTIPLE_HIERARCHIES
// （上向き探索は最初の基底で停止せず 2 基底へ到達する）
@Enumize
sealed interface MultNestedMid {
    @Enumize
    sealed interface Mid : MultNestedMid {
        data object MLeaf : Mid
    }
}
