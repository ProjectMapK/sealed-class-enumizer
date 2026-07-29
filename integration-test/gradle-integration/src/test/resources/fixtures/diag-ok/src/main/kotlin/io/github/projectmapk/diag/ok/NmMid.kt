package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-36 用: 中間 sealed（kind でなく label を持たない）
@Enumize
sealed interface NmMid {
    sealed interface Same : NmMid
}
