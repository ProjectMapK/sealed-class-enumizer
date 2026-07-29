package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-51: 手動 Enumized<自身の Enumish>（型引数一致）→ 注入スキップ・非発火
@Enumize
sealed interface NmSelf : Enumized<NmSelf.Enumish> {
    data object L : NmSelf
}
