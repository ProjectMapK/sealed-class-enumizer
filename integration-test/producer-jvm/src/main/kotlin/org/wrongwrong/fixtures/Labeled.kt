package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// toString の 2 原則（docs/概要.md §4）と label の拡張シャドーイング（ENUMIZE_EXTENSION_SHADOWED 警告）
@Enumize
sealed interface Labeled {
    // メンバー label が Enumized<T>.label 拡張を呼び出し点でシャドーイングする（警告・エラーではない）
    data class Tagged(val label: String) : Labeled

    // kind（末端 object）自身の手動 toString — プラグインは生成しない（原則 1）
    data object Manual : Labeled {
        override fun toString(): String = "custom"
    }

    // kind（companion）の手動 toString — プラグインは生成しない（原則 1）
    data class Styled(val v: Int) : Labeled {
        companion object {
            override fun toString(): String = "companion-custom"
        }
    }
}
