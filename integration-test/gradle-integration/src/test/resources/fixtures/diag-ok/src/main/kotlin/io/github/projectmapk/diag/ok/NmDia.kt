package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-16: 単一階層内ダイヤモンド（基底直接 + 中間経由）→ 非発火
@Enumize
sealed interface NmDia {
    sealed interface Mid : NmDia

    data object X : NmDia, Mid
}
