package org.wrongwrong.fixtures.guarded

import org.wrongwrong.sealedClassEnumizer.Enumize

// sealed class 基底 + protected ネスト末端（TC-VIS-017）。
// protected 末端も基底本体スコープ（entries 構築）から参照可能で ENUMIZE_KIND_NOT_ACCESSIBLE 非発火。
// companion の実効可視性 = 末端と同等 → 規則 1（具体型）
@Enumize
sealed class Guarded {
    protected class Inner : Guarded() {
        companion object
    }

    data object Open : Guarded()

    companion object {
        fun makeInner(): Guarded = Inner()
    }
}
