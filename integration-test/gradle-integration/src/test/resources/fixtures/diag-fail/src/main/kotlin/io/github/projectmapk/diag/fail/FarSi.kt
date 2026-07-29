package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-65 用の基底（別パッケージ末端の言語委譲）
@Enumize
sealed interface FarSi {
    data object Near : FarSi
}
