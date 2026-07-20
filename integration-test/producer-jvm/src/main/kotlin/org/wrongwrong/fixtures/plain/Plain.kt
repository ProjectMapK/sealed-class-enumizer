package org.wrongwrong.fixtures.plain

import org.wrongwrong.sealedClassEnumizer.Enumize

// final class 末端（明示 companion）と既存 companion への生成メンバー共存のフィクスチャ
// （TC-LEAF-001 / TC-LEAF-020 / TC-LEAF-024 / TC-LEAF-058。非 data の object 末端 = TC-LEAF-005 / TC-LEAF-064）
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
}
