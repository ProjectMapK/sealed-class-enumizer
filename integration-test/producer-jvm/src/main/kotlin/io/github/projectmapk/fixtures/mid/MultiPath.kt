package io.github.projectmapk.fixtures.mid

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 多重経路（複数の中間 sealed を同時に実装する末端）のフィクスチャ基底
// （docs/test/ケース01-生成と実行時API.md API-57）。
// 継承者はファイル分散: MpLeft.kt / MpRight.kt（兄弟中間）と各末端ファイル。
// RootVia とは独立した階層とし、entries スナップショットを相互に汚さない
@Enumize sealed interface MultiPath
