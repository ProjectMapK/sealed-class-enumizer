package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-50: Enumized<他階層 Enumish> の直接実装は MSM であって MH ではない

// 末端 TfCross が属する側
@Enumize
sealed interface TfA {
    data object La : TfA
}

// 生成 Enumish を型引数として持ち込まれる側
@Enumize
sealed interface TfB {
    data object Lb : TfB
}

data object TfCross : TfA, Enumized<TfB.Enumish>
