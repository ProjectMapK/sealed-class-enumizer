package io.github.projectmapk.sweep.xnuse

import io.github.projectmapk.sweep.xn.SwXnMan

// docs/test/ケース05-境界横断.md XMP-13: internal な階層内手動実装を含む階層は、手動実装の
// is 枝を書けない（不可視）ため else 無し kind-when が網羅不成立になる
fun useMan(x: SwXnMan): String = when (x.asEnumish()) {
    SwXnMan.Only -> "only"
}
