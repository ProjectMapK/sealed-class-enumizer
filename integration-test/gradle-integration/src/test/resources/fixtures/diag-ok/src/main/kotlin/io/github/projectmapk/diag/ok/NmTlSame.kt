package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-52: 同一ファイル別名（エイリアスが先に解決されない配置は raw 追跡で照合）。
// 別名と階層の同居がテストの本質のため 1 ファイル 1 クラス規約は適用外
typealias NmTlSameAlias = NmTlSameSi.Enumish

@Enumize
sealed interface NmTlSameSi {
    data object L : NmTlSameSi, NmTlSameAlias
}
