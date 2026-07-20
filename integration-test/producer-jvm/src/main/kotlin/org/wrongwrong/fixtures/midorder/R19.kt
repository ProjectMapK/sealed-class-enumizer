package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 複数兄弟中間 sealed・ネスト / トップレベル混在（TC-ORD-019）。
// 継承者 [D19, M1, M2] → 展開 → entries = [D19, A19, B19, C19]
@Enumize
sealed interface R19 {
    sealed interface M1 : R19 {
        data object A19 : M1

        data object B19 : M1
    }

    sealed interface M2 : R19
}
