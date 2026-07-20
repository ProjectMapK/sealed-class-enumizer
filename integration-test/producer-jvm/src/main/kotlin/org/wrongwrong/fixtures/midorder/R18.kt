package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed は基底にネスト・その末端はトップレベル（TC-ORD-018）。
// 中間の配置ではなく末端の配置が break を決める: entries = [Bbb18, Aaa18]
@Enumize
sealed interface R18 {
    sealed interface Mid : R18
}
