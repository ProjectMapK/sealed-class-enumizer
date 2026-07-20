package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed 配下で末端がネストとトップレベル混在の部分 break（TC-ORD-051 訂正メモ準拠）。
// SI51 継承者 [Mid, Top51] → Mid 継承者は FQN 正規化で [Outside51, Inside]（'O' < 'S'）
// → entries = [Outside51, Inside, Top51]
@Enumize
sealed interface SI51 {
    sealed interface Mid : SI51 {
        data object Inside : Mid
    }
}
