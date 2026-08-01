package io.github.projectmapk.diag.xlib

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-22 用の 2 階層（利用側 app の Cross2 が両末端を実装する）

@Enumize
sealed interface XfamSi1 {
    interface LeafC : XfamSi1
}

@Enumize
sealed interface XfamSi2 {
    interface LeafD : XfamSi2
}
