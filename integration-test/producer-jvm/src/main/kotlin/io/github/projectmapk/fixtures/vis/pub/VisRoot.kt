package io.github.projectmapk.fixtures.vis.pub

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 可視性合成階層の基底（docs/test/ケース02-可視性.md §2〜§4・§6・docs/test/ケース03-順序.md ORD-10）。
// 本ファイルは同一ファイル配置がケースの要請となる宣言（同一ファイル private トップレベル末端・
// private トップレベル中間とその末端）を同居させるため、1 ファイル 1 クラス規約の適用外とする。
//
// 期待 entries（label 列。FQN UTF-16 序数・ケース03 §1 の規則から導出）:
// [AutoLeaf, ClsLeaf, CmdFall, EnLeaf, FarInternal, FarSecret, Forged, Half, HostedLeaf, MA,
//  NearSecret, ObLeaf, PLeaf, PrivComp, ProtComp, ValFall, NestedPrivComp, NestedSecret, VvLeaf]
@Enumize
sealed interface VisRoot {
    // 基底ネストの private 末端（壁なし: entries 構築コード=基底スコープから直接参照可。VIS-16）
    private data object NestedSecret : VisRoot

    // 基底ネスト末端の private companion（ネスト壁: 末端クラス内の IR-only アクセサ経由で掲載。VIS-17）
    class NestedPrivComp(val v: Int) : VisRoot {
        private companion object
    }

    // NG 固定（docs/test/ケース02-可視性.md VIS-07 関連）: sealed interface 基底のネストメンバーへ
    // internal 指定は言語上構成できない（e: Modifier 'internal' is not applicable inside 'interface'。
    // interface メンバーの可視性は public / private のみ）:
    //
    //   internal data object NgNested : VisRoot
}

// --- internal 末端の各種別（VIS-07。トップレベル internal は module 内で名指し可・壁なしの直接参照） ---

internal object ObLeaf : VisRoot

internal enum class EnLeaf : VisRoot {
    A,
    B;

    internal companion object
}

@JvmInline internal value class VvLeaf(val x: Int) : VisRoot

internal class ClsLeaf(val v: Int) : VisRoot {
    internal companion object
}

internal class AutoLeaf(val v: Int) : VisRoot

// --- 同一ファイルの private トップレベル末端（壁なし対照。VIS-16） ---

private class NearSecret(val v: Int) : VisRoot

// --- private トップレベル中間とその末端（VIS-21: 中間は生成コードの参照対象外＝アクセサ不要でビルド成立。
//     private トップレベルはファイル内可視のため中間と末端は本ファイル配置が言語上の要請） ---

private sealed interface PMid : VisRoot

private data object PLeaf : PMid

// NG 固定（docs/test/ケース02-可視性.md VIS-15）: interface / fun interface 末端の internal companion は
// 言語上構成できない（e: Modifier 'internal' is not applicable inside 'interface'）:
//
//   interface IfNg : VisRoot { internal companion object }
//   fun interface FnNg : VisRoot { fun run(x: Int): Int; internal companion object }
//
// 対照（public companion の default asEnumish）はケース01 の zoo が担う
