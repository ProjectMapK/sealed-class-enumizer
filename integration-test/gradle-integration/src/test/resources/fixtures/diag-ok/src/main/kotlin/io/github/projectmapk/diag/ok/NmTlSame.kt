package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-52: 同一ファイル別名（エイリアスが先に解決されない配置は raw 追跡で照合）。
// 別名と階層を同一ファイルへ置くことそのものが本ケースの成立条件である
// （先に解決される別ファイル配置は TypealiasAliases.kt / TypealiasMatch.kt が担う）
typealias NmTlSameAlias = NmTlSameSi.Enumish

@Enumize
sealed interface NmTlSameSi {
    data object L : NmTlSameSi, NmTlSameAlias
}
