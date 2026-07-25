package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-095: 単一階層内のダイヤモンド（基底直接 + 中間 sealed 経由） → MULTIPLE_FAMILIES 非発火
@Enumize
sealed interface NmDia {
    sealed interface Mid : NmDia

    data object X : NmDia, Mid
}
