package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-31: 外側スコープのネスト解決（supertype 名が外側宣言のスコープで解決される）
object NmOutHost {
    @Enumize
    sealed interface NBase

    class NLeaf(val v: Int) : NBase
}
