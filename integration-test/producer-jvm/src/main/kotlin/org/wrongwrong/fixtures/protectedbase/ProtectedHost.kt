package org.wrongwrong.fixtures.protectedbase

import org.wrongwrong.sealedClassEnumizer.Enumize

// protected ネスト基底（TC-VIS-008・docs/エッジケースへの対応方針.md §1.2）。
// 可視範囲は ProtectedHost とそのサブクラス（別モジュールを含む）。
// 別モジュールのサブクラス文脈からの利用は consumer-pure-jvm 側で観測する
open class ProtectedHost {
    @Enumize
    protected sealed interface Shielded {
        data object On : Shielded

        data class Off(val code: Int) : Shielded
    }
}
