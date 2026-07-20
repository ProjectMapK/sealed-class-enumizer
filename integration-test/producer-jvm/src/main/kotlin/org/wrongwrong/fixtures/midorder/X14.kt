package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed が兄弟の中間位置にある in-place 展開（TC-ORD-014）。
// 継承者 [Aaa14, Mid14, Zzz14] → Mid14 位置を [P, Q] で置換 → entries = [Aaa14, P, Q, Zzz14]
@Enumize
sealed interface X14
