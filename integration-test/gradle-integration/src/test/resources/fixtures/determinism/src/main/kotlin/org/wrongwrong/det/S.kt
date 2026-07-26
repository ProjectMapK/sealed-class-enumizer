package org.wrongwrong.det

import org.wrongwrong.sealedClassEnumizer.Enumize

// 決定性フィクスチャの基底（docs/概要.md §5・docs/test/ケース06-ビルド動態.md §1）。
// ネスト末端・可視性混在・toString 2 原則の対象を同居させる
@Enumize
sealed interface S {
    // 原則 1(c): data object は言語合成の toString を保つ（生成しない）
    data object Aaa : S

    // 原則 1(a): kind（companion）の手動 toString には生成しない
    data class Custom(val raw: String) : S {
        companion object {
            override fun toString(): String = "custom!"
        }
    }

    // 基底ネストの private 末端（kind は基底本体から参照可 = 診断対象外。順序は可視性に無関係）
    private data object Priv : S
}
