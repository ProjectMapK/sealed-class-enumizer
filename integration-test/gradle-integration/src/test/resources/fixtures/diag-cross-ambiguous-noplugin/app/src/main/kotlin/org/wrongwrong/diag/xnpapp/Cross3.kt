package org.wrongwrong.diag.xnpapp

import org.wrongwrong.diag.xnp.XnpSi

// TC-DIAG-096: プラグイン未適用の利用側での複数末端実装 → 言語エラーのみ（ENUMIZE_AMBIGUOUS_KIND は非発火）
class Cross3 : XnpSi.LeafA, XnpSi.LeafB
