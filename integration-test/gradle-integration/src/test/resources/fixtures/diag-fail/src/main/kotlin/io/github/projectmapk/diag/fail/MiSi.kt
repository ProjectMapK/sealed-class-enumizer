package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-56 用の基底
@Enumize
sealed interface MiSi {
    data object Ok : MiSi
}
