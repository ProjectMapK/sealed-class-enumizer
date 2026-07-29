package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-05: 正値の sealed class 階層（DIA-16 の独立 2 階層の片割れを兼ねる）
@Enumize
sealed class OkSc {
    data object C : OkSc()
}
