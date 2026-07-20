package org.wrongwrong.fixtures.narrow

import org.wrongwrong.sealedClassEnumizer.Enumize

// internal 基底 + 基底内ネストの public 宣言末端（TC-VIS-058）。
// ネスト包含により eff(Inner) = min(public, internal) = internal で「基底より広い末端」に該当せず、
// eff(C) = internal = eff(L) → 規則 1（具体型）。トップレベル配置（widerleaf）との配置境界
@Enumize
internal sealed interface NestHost {
    class Inner(val v: Int) : NestHost {
        internal companion object
    }
}
