package io.github.projectmapk.fixtures.vis.nestbase

import io.github.projectmapk.sealedClassEnumizer.Enumize

// open class 内の protected ネスト基底（docs/test/ケース02-可視性.md VIS-03。
// 可視範囲はホスト本体とそのサブクラス（別モジュールを含む）。
// 別モジュールのサブクラス文脈からの利用はケース05（consumer 側）が担う）
open class ProtectedHost {
    @Enumize
    protected sealed interface Shielded {
        data object On : Shielded

        data class Off(val code: Int) : Shielded
    }
}
