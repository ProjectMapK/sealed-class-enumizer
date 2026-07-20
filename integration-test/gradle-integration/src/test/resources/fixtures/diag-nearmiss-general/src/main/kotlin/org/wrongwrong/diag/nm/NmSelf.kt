package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-DIAG-052: 手動 : Enumized<自身の Enumish>（型引数一致） → 非発火（注入スキップ・生成は通常どおり）
@Enumize
sealed interface NmSelf : Enumized<NmSelf.Enumish> {
    data object L : NmSelf
}
