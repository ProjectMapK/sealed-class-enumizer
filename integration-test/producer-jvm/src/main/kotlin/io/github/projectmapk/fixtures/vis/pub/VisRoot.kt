package io.github.projectmapk.fixtures.vis.pub

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 可視性合成階層の基底（docs/test/ケース02-可視性.md §2〜§4・§6・docs/test/ケース03-順序.md ORD-10）。
// 本ファイルは基底から名前参照できる末端（= IR-only アクセサ不要）と、ネスト壁を作る companion を担う。
// 基底のファイル外へ置くこと自体が要請となる末端（別ファイル internal = 壁なし対照・
// 別ファイル private トップレベル = トップレベル壁）は VisRootFarLeaves.kt が担う。
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

// --- 規則 2（eff(C) < eff(L) で asEnumish の返り値型が VisRoot.Enumish へフォールバック） ---

// public 末端 × internal 既定名 companion（VIS-12）
class Half(val v: Int) : VisRoot {
    internal companion object
}

// public 末端 × internal 名前つき companion（VIS-13。label は companion 名でなく末端単純名 "Forged" のまま不変）
class Forged(val v: Int) : VisRoot {
    internal companion object Factory
}

// public value class 末端 × internal companion（VIS-14 = 規則 2 × 種別。boxing を跨いでも kind は同一のまま）
@JvmInline
value class ValFall(val x: Int) : VisRoot {
    internal companion object
}

// public enum class 末端 × internal companion（VIS-14 = 規則 2 × 種別。
// enum 全体で 1 kind・name と label の分離は維持される）
enum class CmdFall : VisRoot {
    GO,
    STOP;

    internal companion object
}

// NG 固定（VIS-15）: interface / fun interface 末端の internal companion は
// 言語上構成できない（e: Modifier 'internal' is not applicable inside 'interface'）:
//
//   interface IfNg : VisRoot { internal companion object }
//   fun interface FnNg : VisRoot { fun run(x: Int): Int; internal companion object }
//
// 対照（public companion の default asEnumish）はケース01 の zoo が担う

// --- ネスト壁（VIS-17）: 壁の親クラス内 `$EnumizeKindAccessor` 経由で掲載され、返り値型は規則 2 ---

// トップレベル末端 × private companion
class PrivComp(val v: Int) : VisRoot {
    private companion object
}

// トップレベル末端 × protected companion（protected companion は class 内でのみ言語上構成できる。
// 非 final 末端 × private companion の組はケース05 の mpp kindaccessor が担う）
class ProtComp(val v: Int) : VisRoot {
    protected companion object
}

// --- 同一ファイルの private トップレベル末端（壁なし対照。VIS-16） ---

private class NearSecret(val v: Int) : VisRoot

// --- 中間の可視性（VIS-21。中間には何も生成されず、その可視性は生成・掲載・末端 kind 解決に影響しない） ---

// internal な中間 sealed
internal sealed interface HiddenMid : VisRoot

// internal 中間経由の public data object 末端。
// 露出検査は interface 実装に働かないため、internal 中間より広い public 末端が言語上成立する
// = docs/エッジケースへの対応方針.md §1.1 #2 の成立形
data object MA : HiddenMid

// private トップレベル中間とその末端（中間は生成コードの参照対象外＝アクセサ不要でビルド成立。
// private トップレベルはファイル内可視のため中間と末端は同一ファイル配置が言語上の要請）

private sealed interface PMid : VisRoot

private data object PLeaf : PMid
