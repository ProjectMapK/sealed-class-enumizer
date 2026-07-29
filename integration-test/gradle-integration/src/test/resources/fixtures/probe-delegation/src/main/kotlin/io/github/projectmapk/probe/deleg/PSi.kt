package io.github.projectmapk.probe.deleg

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-69 用の基底
@Enumize
sealed interface PSi {
    data object Ok : PSi
}
