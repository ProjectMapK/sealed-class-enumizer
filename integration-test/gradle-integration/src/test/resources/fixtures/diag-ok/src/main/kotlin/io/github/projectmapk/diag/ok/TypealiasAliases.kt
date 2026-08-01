package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-52 用の別名宣言。
// 頭別名（NmThAlias）は基底より先に解決される配置であることが要件であり、
// 解決順はファイル名順に従うため本ファイル名は TypealiasMatch.kt より前に来る必要がある
// （先に解決されない配置は言語側がバックエンド ICE になる = sweep-typealias-samefile-head の既知の制限）

// 型引数別名
typealias NmTaAlias = NmTaSi.Enumish

// 手動 supertype の頭（Enumized 自体）への別名
typealias NmThAlias = Enumized<NmThSi.Enumish>

// 末端側の冗長宣言に使う別名
typealias NmTlAlias = NmTlSi.Enumish
