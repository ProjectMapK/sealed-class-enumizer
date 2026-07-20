package org.wrongwrong.fixtures.fallback

import org.wrongwrong.sealedClassEnumizer.Enumize

// 各末端種別 × internal companion × 規則 2 フォールバックの合成
// （TC-GAP-012 = value class / TC-GAP-013 = enum class）。
//
// NG（TC-GAP-010 / TC-GAP-011）: interface / fun interface 末端の「internal companion」は言語上構成できない
// （e: Modifier 'internal' is not applicable inside 'interface'。interface メンバーの可視性は public / private のみ）。
// private companion は ENUMIZE_KIND_NOT_ACCESSIBLE（TC-GAP-019）になるため、interface 系末端で
// 規則 2（eff(C) < eff(L) の companion）は到達不能である。最小再現（コメントアウト解除でコンパイルエラー）:
//
//   interface Custom : FallbackHost { internal companion object }
//   fun interface Fn : FallbackHost { fun run(x: Int): Int; internal companion object }
//
// docs/修正方針案.md 反映待ち。以下は companion 可視性を既定（public）へ落とした対照フィクスチャ
@Enumize
sealed interface FallbackHost {
    interface Custom : FallbackHost {
        companion object
    }

    fun interface Fn : FallbackHost {
        fun run(x: Int): Int

        companion object
    }

    @JvmInline
    value class Val(val x: Int) : FallbackHost {
        internal companion object
    }

    enum class Cmd : FallbackHost {
        HELP,
        VERSION,
        ;

        internal companion object
    }
}
