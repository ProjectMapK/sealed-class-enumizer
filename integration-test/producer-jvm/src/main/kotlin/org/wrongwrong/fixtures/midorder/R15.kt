package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed が中間位置・継承者はトップレベル（TC-ORD-015）。
// 継承者 [Bbb15, Mid15, Yyy15] → 展開 → entries = [Bbb15, Aaa15, Yyy15]（末端集合の FQN 順にならない）
@Enumize sealed interface R15
