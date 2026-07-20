package org.wrongwrong.fixtures.reentry

import org.wrongwrong.sealedClassEnumizer.Enumize

// 初期化再入の禁止（TC-BOX-070）: companion（kind）の初期化子から valueOfOrNull を参照する構成
@Enumize
sealed interface ReValueOfOrNull {
    class Leaf(val v: Int) : ReValueOfOrNull {
        companion object {
            val eager: Any? = ReValueOfOrNull.Enumish.valueOfOrNull("Leaf")
        }
    }
}
