package org.wrongwrong.fixtures.fallback

import org.wrongwrong.sealedClassEnumizer.Enumize

// 各末端種別 × internal companion × 規則 2 フォールバックの合成
// （TC-GAP-012 = value class / TC-GAP-013 = enum class）。
//
// interface / fun interface 末端の「internal companion」は言語上構成できない
// （e: Modifier 'internal' is not applicable inside 'interface'。interface メンバーの可視性は public / private のみ）:
//
//   interface Custom : FallbackHost { internal companion object }
//   fun interface Fn : FallbackHost { fun run(x: Int): Int; internal companion object }
//
// private companion なら IR-only アクセサで entries に載り asEnumish は規則 2 へ至る（TC-GAP-019）。
// 以下は companion 可視性を既定（public）へ落とし、interface 末端の default asEnumish を観測する対照フィクスチャ
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
