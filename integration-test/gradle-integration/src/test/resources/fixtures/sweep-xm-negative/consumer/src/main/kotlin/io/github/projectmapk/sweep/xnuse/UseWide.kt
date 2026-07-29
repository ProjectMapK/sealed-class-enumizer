package io.github.projectmapk.sweep.xnuse

import io.github.projectmapk.sweep.xn.SwXnWideBase
import io.github.projectmapk.sweep.xn.SwXnWideLeaf

// docs/test/ケース05-境界横断.md XMP-17: 基底より広い末端でも、kind 単位の網羅 when の土台となる sealed 親
// （SwXnWideBase.Enumish）が internal で不可視のため、外側では階層単位の when を構成できない
// （= sealed 親の名指し自体が言語の可視性エラー）
fun useWideDenote(l: SwXnWideLeaf): SwXnWideBase.Enumish = l.asEnumish()

// 実測記録: asEnumish() の静的型は規則 1 の具体型（SwXnWideLeaf.Companion = object 型）のため、
// 単一 kind に閉じた when は object 型の網羅として else 無しでも成立する（階層単位の網羅とは別物）
fun useWideSingle(l: SwXnWideLeaf): String = when (l.asEnumish()) {
    SwXnWideLeaf.Companion -> "wide"
}
