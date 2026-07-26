package org.wrongwrong.fixtures.vis.narrow

import org.wrongwrong.sealedClassEnumizer.Enumize

// internal 基底（docs/test/ケース02-可視性.md VIS-02/VIS-06/VIS-19。
// module 内では全 API が通常どおり使える = 基底の可視性は制限ではない）
//
// NG 固定（docs/test/ケース02-可視性.md VIS-19）: E-2（基底より広い末端）が成立するのは class 系末端のみ。
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
