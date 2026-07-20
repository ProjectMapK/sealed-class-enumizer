package org.wrongwrong.fixtures.sealedbase

import org.wrongwrong.sealedClassEnumizer.Enumize

// sealed class 基底のフィクスチャ（TC-LEAF-076 / TC-LEAF-078）。
// 構築子呼び出し形 `:Task()` の supertype からも companion 自動生成（V3）が成立する
@Enumize
sealed class Task {
    // companion 明示なし・構築子呼び出し supertype（TC-LEAF-076）
    data class Run(val v: Int) : Task()

    // 非 data object 末端（toString = label が生成される）
    object Plain : Task()

    // data object 末端（toString は言語合成のまま）
    data object Done : Task()
}
