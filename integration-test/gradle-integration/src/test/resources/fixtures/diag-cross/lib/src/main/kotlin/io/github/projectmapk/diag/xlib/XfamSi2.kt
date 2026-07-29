package io.github.projectmapk.diag.xlib

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-22 用の階層 2
@Enumize
sealed interface XfamSi2 {
    interface LeafD : XfamSi2
}
