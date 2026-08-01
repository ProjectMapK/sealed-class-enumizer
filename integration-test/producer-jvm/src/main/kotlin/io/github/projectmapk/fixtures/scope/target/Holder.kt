package io.github.projectmapk.fixtures.scope.target

import io.github.projectmapk.sealedClassEnumizer.Enumize

// raw 追跡スコープ順フィクスチャの真基底と囮（docs/test/ケース01-生成と実行時API.md API-51）。
// 単純名 "Base" が object Holder 内ネストの @Enumize 基底と同一 pkg のトップレベル囮の双方へ解決しうる状態を作る。
// 競合 3 形は末端側のファイルが担い、各ファイルの import 集合がそのまま解決文脈となるため
// ViaImport.kt / ViaStar.kt / ShellHost.kt は分割を保つ
object Holder {
    @Enumize sealed interface Base
}

// 囮のトップレベル非 sealed interface。@Enumize ではなく、raw 表記 "Base" の解決先競合を作るためだけに存在する
interface Base
