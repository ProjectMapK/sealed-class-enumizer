package org.wrongwrong.mpp.fixtures.tostring

import org.wrongwrong.sealedClassEnumizer.Enumize

// kind の toString 2 原則の混在フィクスチャ（docs/概要.md §4・docs/test/ケース05-境界横断.md XMP-35）。
// 生成対象（Plain）と明示実装持ち（手動 = Manual/Styled・継承具象 = Inherited・data object 合成 = Auto）を
// 混在させ、IR-only 生成（V11）が klib 直列化と各バックエンドの仮想ディスパッチを通ることを観測する
@Enumize
sealed interface Show {
    // 明示実装なし → companion に toString() = label が生成される
    data class Plain(val v: Int) : Show

    // data object の言語合成 toString（単純名）→ 生成しない
    data object Auto : Show

    // kind（末端 object）自身の手動 toString → 生成しない
    data object Manual : Show {
        override fun toString(): String = "manual!"
    }

    // kind（companion）の手動 toString → 生成しない
    data class Styled(val v: Int) : Show {
        companion object {
            override fun toString(): String = "styled!"
        }
    }

    // kind（companion）が継承経路上に Any 以外の具象 toString を持つ → 生成しない
    data class Inherited(val v: Int) : Show {
        companion object : Loud()
    }
}
