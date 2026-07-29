package io.github.projectmapk.diag.xlib

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-57 用の基底
@Enumize
sealed interface XrgSi {
    data object Ok : XrgSi
}
