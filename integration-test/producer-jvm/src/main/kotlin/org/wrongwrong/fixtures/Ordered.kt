package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// entries 順序フィクスチャ（TC-BOX-004）: 宣言順を単純名昇順と逆に書き、
// entries が FQN 序数順（= この構成では単純名昇順）であることを観測する
@Enumize
sealed interface Ordered {
    data object Zzz : Ordered

    data object Mmm : Ordered

    data object Aaa : Ordered
}
