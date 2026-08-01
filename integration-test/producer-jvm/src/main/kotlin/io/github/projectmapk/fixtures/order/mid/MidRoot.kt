package io.github.projectmapk.fixtures.order.mid

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 中間 sealed の DFS 入れ子展開フィクスチャ（docs/test/ケース03-順序.md §2.2・ORD-03/ORD-05/ORD-06）。
// 中間の配置（トップレベル / 基底ネスト）と末端の配置（中間ネスト / トップレベル）を組み合わせ、
// 全末端 FQN 一括整列との break を作る。
//
// 直接継承者の整列 = [Aaa, MidMulti, MidNest, MidRoot.MidIn, MidTop, Probe, Probe.Bb, Zzz]
// 期待 entries（label 列。中間をその位置で深さ優先に入れ子展開）:
// [Aaa, Bottom, Wide, NestA, NestB, Outpost, Early, Late, Probe, Bb, Zzz]
// 全末端 FQN 一括整列との対比（MidTop 配下と Wide が break を示す）:
// [Aaa, Early, Late, Bottom, NestA, NestB, Outpost, Probe, Bb, Wide, Zzz]
@Enumize
sealed interface MidRoot {
    // 基底ネスト配置の中間 sealed（配下の末端 Outpost はトップレベル配置）
    sealed interface MidIn : MidRoot
}

// --- 直接末端（in-place 展開位置の前後） ---

data object Aaa : MidRoot

data object Zzz : MidRoot

// --- トップレベル中間 × ネスト末端（局所展開列 [NestA, NestB] が全末端 FQN 順とも一致する側） ---

sealed interface MidNest : MidRoot {
    data object NestA : MidNest

    data object NestB : MidNest
}

// --- トップレベル中間 × トップレベル末端（配下の Early / Late は展開位置に留まり break を作る） ---

sealed interface MidTop : MidRoot

data object Early : MidTop

data object Late : MidTop

// --- 多段中間（トップレベル中間 → ネスト中間 Deep）。
//     Deep 配下はネスト末端 Bottom とトップレベル末端 Wide（深部 break）で構成する ---

sealed interface MidMulti : MidRoot {
    sealed interface Deep : MidMulti {
        data object Bottom : Deep
    }
}

data object Wide : MidMulti.Deep

// --- 基底ネスト中間 MidRoot.MidIn 配下のトップレベル末端（中間配置 = 基底ネスト値） ---

data object Outpost : MidRoot.MidIn

// --- inheritors 分岐プローブ（ORD-06）: class 末端 Probe（自動生成 companion が kind）と、
//     その内側ネストの data object 末端 Bb。
//     kind ClassId 整列（inheritors 順）では Probe.Bb('B'=66) が Probe.Companion('C'=67) に先行する一方、
//     entries（末端 ClassId 由来）では Probe が Bb に先行し、相対順が分岐する ---

class Probe : MidRoot {
    data object Bb : MidRoot
}
