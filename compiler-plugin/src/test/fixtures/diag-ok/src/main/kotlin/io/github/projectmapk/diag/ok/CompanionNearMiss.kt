package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-30: 階層外クラスの companion 単独末端・SI 非実装の通常 companion は
// COMPANION_LEAF_CONFLICT 非発火

@Enumize
sealed interface NmHostBase

// 階層外クラスの companion が単独で末端（外側 = 末端ではないため非発火）
class NmHost {
    companion object : NmHostBase
}

// SI 非実装の通常 companion は判定対象外
class NmPlain {
    companion object
}
