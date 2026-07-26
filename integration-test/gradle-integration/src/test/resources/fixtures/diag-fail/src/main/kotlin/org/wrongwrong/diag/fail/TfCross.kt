package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-50: Enumized<他階層 Enumish> の直接実装は MSM であって MF ではない
data object TfCross : TfA, Enumized<TfB.Enumish>
