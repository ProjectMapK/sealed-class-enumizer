package io.github.projectmapk.diag.ok.visibility

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: 3 段規則の成立形（規則 1 / 2・広い末端 object・private 基底・
// 基底ネスト private 末端）は KIND_TYPE_NOT_DENOTABLE 非発火（C2 の分岐網羅）。
// 別ファイル配置が要件となる private トップレベル中間は NmVis3Mid.kt が担う

// 基底内ネストの private 末端
@Enumize
sealed interface NmVis1 {
    private data object Hidden : NmVis1

    data object Shown : NmVis1
}

// private 基底と private 末端（private トップレベルはファイルスコープのため同一ファイル配置が要件）
@Enumize
private sealed interface NmVis2

private data object NmVis2Leaf : NmVis2

// 別ファイルの private トップレベル中間 sealed を持つ基底
@Enumize
sealed interface NmVis3

// private 基底 + internal companion（実効可視性で規則 1）
@Enumize
private sealed interface NmVis4 {
    class L(val v: Int) : NmVis4 {
        internal companion object
    }
}

// internal 基底 + 基底より広い末端 object（kind は自身のため規則対象外）
@Enumize
internal sealed interface NmVis5

object NmVis5Wide : NmVis5

// internal 基底 + 広い末端 + public companion（規則 1 成立）
@Enumize
internal sealed interface NmVis6

class NmVis6Wide : NmVis6 {
    companion object
}

// public 基底 + internal companion → 規則 2 フォールバック
@Enumize
sealed interface NmVis7 {
    class Half(val v: Int) : NmVis7 {
        internal companion object
    }
}
