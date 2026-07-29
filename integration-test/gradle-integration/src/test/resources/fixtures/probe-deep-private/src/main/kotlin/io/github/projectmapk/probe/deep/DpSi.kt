package io.github.projectmapk.probe.deep

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-68 用の基底
@Enumize
sealed interface DpSi {
    data object Open : DpSi
}
