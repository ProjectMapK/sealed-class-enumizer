package io.github.projectmapk.diag.xapp2

import io.github.projectmapk.diag.xlib.XambSi

// docs/test/ケース04-診断.md DIA-21: プラグイン未適用の利用側での複数末端実装
// → 言語 MANY_IMPL_MEMBER のみ（ENUMIZE_AMBIGUOUS_KIND は非発火 = 診断は適用側限定）
class Cross3 : XambSi.LeafA, XambSi.LeafB
