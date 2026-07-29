package io.github.projectmapk.diag.mppf

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-09 用の基底（非 expect の通常宣言）
@Enumize
sealed interface MelSi {
    data object MBase : MelSi
}
