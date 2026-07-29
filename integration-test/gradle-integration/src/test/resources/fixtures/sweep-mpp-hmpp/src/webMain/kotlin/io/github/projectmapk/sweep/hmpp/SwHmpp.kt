package io.github.projectmapk.sweep.hmpp

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-11 用の基底（中間ソースセット webMain に配置）
@Enumize
sealed interface SwHmpp {
    data object W : SwHmpp
}
