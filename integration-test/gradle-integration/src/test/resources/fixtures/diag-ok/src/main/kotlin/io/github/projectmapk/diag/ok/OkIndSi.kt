package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-51: 自作 interface 経由の間接一致 → 注入スキップ・非発火
@Enumize
sealed interface OkIndSi : OkIndBase {
    data object L : OkIndSi
}
