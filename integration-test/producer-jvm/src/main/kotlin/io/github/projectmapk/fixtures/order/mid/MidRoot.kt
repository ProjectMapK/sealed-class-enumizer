package io.github.projectmapk.fixtures.order.mid

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 中間 sealed の DFS 入れ子展開フィクスチャ基底（docs/test/ケース03-順序.md §2.2・ORD-03/ORD-05/ORD-06）。
//
// 直接継承者の整列 = [Aaa, MidMulti, MidNest, MidRoot.MidIn, MidTop, Probe, Probe.Bb, Zzz]
// 期待 entries（label 列。中間をその位置で深さ優先に入れ子展開）:
// [Aaa, Bottom, Wide, NestA, NestB, Outpost, Early, Late, Probe, Bb, Zzz]
// 全末端 FQN 一括整列との対比（MidTop 配下と Wide が break を示す）:
// [Aaa, Early, Late, Bottom, NestA, NestB, Outpost, Probe, Bb, Wide, Zzz]
@Enumize
sealed interface MidRoot {
    // 基底ネスト配置の中間 sealed（配下の末端 Outpost はトップレベル配置。ORD-03）
    sealed interface MidIn : MidRoot
}
