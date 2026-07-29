package io.github.projectmapk.icscope

import io.github.projectmapk.sealedClassEnumizer.Enumize

// スコープ解決追跡フィクスチャの基底（docs/test/ケース06-ビルド動態.md BLD-45）。
// 基底と直接末端 ScA を同居させる（エイリアス経由でない対照を基底ファイルに置く配置制約）
@Enumize
sealed interface ScBase

// 直接の末端（エイリアス経由の ScAliasLeaf に対する対照）
data object ScA : ScBase
