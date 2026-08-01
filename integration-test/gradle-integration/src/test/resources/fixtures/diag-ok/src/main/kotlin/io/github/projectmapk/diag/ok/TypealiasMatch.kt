package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-52: 照合は typealias 展開後に行われる。
// 別名宣言は基底より先に解決される配置（ファイル名順で前）が要件であり TypealiasAliases.kt が持つ。
// 先に解決されない同一ファイル配置は NmTlSame.kt が担う

// 型引数別名の手動 Enumized（展開後は厳密一致）→ 注入スキップ
@Enumize
sealed interface NmTaSi : Enumized<NmTaAlias> {
    data object L : NmTaSi
}

// 頭別名（Enumized 自体への別名）→ 注入スキップ
@Enumize
sealed interface NmThSi : NmThAlias {
    data object L : NmThSi
}

// 末端 Enumish 冗長宣言の別名形 → 注入スキップ
@Enumize
sealed interface NmTlSi {
    data object L : NmTlSi, NmTlAlias
}
