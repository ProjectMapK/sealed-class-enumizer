package org.wrongwrong.diag.kindaccess

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-077: 可視性混在の結合 — 基底内ネストの private 末端（非発火側）
@Enumize
sealed interface CombSi {
    private data object Inside : CombSi
}
