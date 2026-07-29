package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-50: Enumized<他階層 Enumish> の直接実装は MSM であって MH ではない
data object TfCross : TfA, Enumized<TfB.Enumish>
