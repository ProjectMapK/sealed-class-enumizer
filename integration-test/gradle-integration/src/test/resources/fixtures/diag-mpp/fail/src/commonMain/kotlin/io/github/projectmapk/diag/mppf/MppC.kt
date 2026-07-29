package io.github.projectmapk.diag.mppf

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-10 用の基底（commonMain）
@Enumize
sealed interface MppC {
    data object CLeaf : MppC
}
