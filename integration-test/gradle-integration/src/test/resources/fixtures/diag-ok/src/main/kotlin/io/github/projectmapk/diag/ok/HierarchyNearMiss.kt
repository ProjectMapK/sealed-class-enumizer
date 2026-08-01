package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-16: 併用・ダイヤモンドは MULTIPLE_HIERARCHIES / NESTED 非発火。
// 独立 2 階層の対照は WellFormed.kt の OkSi / OkSc が兼ねる

// 非 @Enumize interface の併用
@Enumize
sealed interface NmCross {
    interface Marker

    data object X : NmCross, Marker
}

// 単一階層内ダイヤモンド（基底直接 + 中間経由）
@Enumize
sealed interface NmDia {
    sealed interface Mid : NmDia

    data object X : NmDia, Mid
}
