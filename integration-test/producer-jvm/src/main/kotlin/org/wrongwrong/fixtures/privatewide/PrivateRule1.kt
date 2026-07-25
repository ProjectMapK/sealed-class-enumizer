package org.wrongwrong.fixtures.privatewide

import org.wrongwrong.sealedClassEnumizer.Enumize

// private 基底の中の末端 + internal companion（TC-VIS-024 = 設計01 §5.4 末尾の明示例）。
// 実効可視性は private 側が優位（eff(C) = eff(L) = private）で規則 1 が成立し、露出もフォールバックも起きない
@Enumize
private sealed interface PrivateRule1 {
    class Leaf(val v: Int) : PrivateRule1 {
        internal companion object
    }
}

internal fun observePrivateRule1Labels(): List<String> =
    PrivateRule1.Enumish.entries.map { it.label }

internal fun privateRule1KindIsConcrete(): Boolean {
    // 規則 1: asEnumish() の返り値型は具体型（Leaf.Companion）のまま — 具体型で受けられること自体が検査
    val kind: PrivateRule1.Leaf.Companion = PrivateRule1.Leaf(1).asEnumish()
    return kind === PrivateRule1.Leaf.Companion
}
