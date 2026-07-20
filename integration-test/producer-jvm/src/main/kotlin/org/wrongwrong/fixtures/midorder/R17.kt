package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 多段中間 sealed（全てネスト）でも FQN 接頭辞連鎖により全体 FQN 序数順と一致する（TC-ORD-017）
@Enumize
sealed interface R17 {
    sealed interface Mid : R17 {
        sealed interface Sub : Mid {
            data object A : Sub

            data object B : Sub
        }
    }

    data object C : R17
}
