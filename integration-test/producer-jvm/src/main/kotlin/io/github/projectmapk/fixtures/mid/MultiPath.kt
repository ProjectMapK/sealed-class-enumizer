package io.github.projectmapk.fixtures.mid

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 多重経路（複数の中間 sealed を同時に実装する末端）のフィクスチャ
// （docs/test/ケース01-生成と実行時API.md API-57）。
// RootVia とは独立した階層とし、entries スナップショットを相互に汚さない
@Enumize sealed interface MultiPath

// 兄弟中間の一方（多重経路末端 MpBoth の初出経路）
sealed interface MpLeft : MultiPath

// 兄弟中間の他方（多重経路末端 MpBoth の 2 度目の到達経路）
sealed interface MpRight : MultiPath

// 2 つの中間 sealed を同時に実装する末端（companion 明示なし = 多重経路での候補判定も観測する）。
// MpLeft 経由の初出位置に 1 回だけ entries へ載る
class MpBoth : MpLeft, MpRight

// MpLeft 単独経路の末端（MpBoth の後続 = 初出位置の対照）
data object MpOnlyLeft : MpLeft

// MpRight 単独経路の末端（MpBoth の重複が除かれた後に続く）
data object MpOnlyRight : MpRight
