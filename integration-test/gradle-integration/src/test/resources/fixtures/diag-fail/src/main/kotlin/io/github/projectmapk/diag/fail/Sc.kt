package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-64 用の sealed class 基底（末端種別境界の確認）
@Enumize
sealed class Sc {
    data object Ok : Sc()
}
