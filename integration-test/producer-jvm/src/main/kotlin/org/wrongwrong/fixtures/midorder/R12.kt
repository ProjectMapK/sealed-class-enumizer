package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 単一中間 sealed・末端が中間の内側にネスト（TC-ORD-012）。
// 継承者 [C, Mid] → Mid を展開して entries = [C, A, B]（ネストにより全体 FQN 序数順とも一致）
@Enumize
sealed interface R12 {
    sealed interface Mid : R12 {
        data object A : Mid

        data object B : Mid
    }

    data object C : R12
}
