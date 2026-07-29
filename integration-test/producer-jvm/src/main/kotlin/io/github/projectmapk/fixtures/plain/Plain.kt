package io.github.projectmapk.fixtures.plain

import io.github.projectmapk.sealedClassEnumizer.Enumize

// companion 状態の合成階層（docs/test/ケース01-生成と実行時API.md API-23/API-24・
// docs/test/ケース03-順序.md ORD-07）。
// 明示既定名 / ユーザーメンバー共存 / 名前つき public / 非 data object 末端を同居させる
@Enumize
sealed interface Plain {
    // final class 末端 + 明示既定名 companion（既存流用・自動生成はスキップ）
    class Simple(val v: Int) : Plain {
        companion object
    }

    // 既存 companion にユーザーメンバーがある場合も、生成メンバー（label / enumizedClass / toString）と共存する
    class Stocked(val v: Int) : Plain {
        companion object {
            val cfg: Int = 1

            fun make(): Stocked = Stocked(cfg)
        }
    }

    // 非 data の object 末端。kind = 自身であり、toString = label が生成される（data object との差分）
    object Marker : Plain

    // public 名前つき companion。Factory が kind になるが label は末端単純名 "Made" で不変（規則 1）
    class Made(val v: Int) : Plain {
        companion object Factory
    }
}
