package io.github.projectmapk.fixtures.vis.narrow

import io.github.projectmapk.sealedClassEnumizer.Enumize

// internal 基底の可視性フィクスチャ（docs/test/ケース02-可視性.md VIS-02/VIS-06/VIS-10/VIS-11/VIS-19）。
// module 内では全 API が通常どおり使える（= 基底の可視性は制限ではない）。
//
// NG 固定（VIS-19）: E-2（基底より広い末端）が成立するのは class 系末端のみ。
// 露出検査は interface の継承（extends 形）に働くため、internal 基底より広い public interface 末端は
// 言語上構成できない（実測: e: 'public' sub-interface exposes its 'internal' supertype 'NarrowBase'）:
//
//   interface WideIface : NarrowBase { companion object }
@Enumize
internal sealed interface NarrowBase {
    // 基底ネストの public 宣言末端（VIS-06: 実効可視性は min(public, internal) = internal で
    // 「基底より広い末端」に該当せず、eff(C) = eff(L) = internal → 規則 1 具体型）
    class Nested(val v: Int) : NarrowBase {
        internal companion object
    }
}

// internal 末端 × internal companion（VIS-10 = 規則 1 境界:
// eff(C) = eff(L) = internal の実効同値で規則 2 は発火せず具体型 NarrowLeaf.Companion のまま）
internal class NarrowLeaf(val v: Int) : NarrowBase {
    internal companion object
}

// 基底（internal）より広い public 末端・companion 明示なし（VIS-11:
// 自動生成 companion は宣言 public・実効可視性が末端へ追随し常に規則 1。露出診断も規則 3 も発火しない）
class AutoWide(val v: Int) : NarrowBase

// 基底（internal）より広い public 末端 × 明示 public companion（VIS-19 = E-2 生成側。
// sealed interface 階層では言語上成立し（診断なし=コンパイル成功が兼ねる）、
// 階層 API（entries / valueOf）は internal・値/kind API（asEnumish / label）は public という分離が生じる）
class PublicLeaf(val v: Int) : NarrowBase {
    companion object
}
