package org.wrongwrong.fixtures.reentry

import org.wrongwrong.sealedClassEnumizer.Enumize

// 初期化再入の禁止（TC-BOX-053）: companion（kind）の初期化子から valueOf を参照する構成
@Enumize
sealed interface ReValueOf {
    class Leaf(val v: Int) : ReValueOf {
        companion object {
            val eager: Any = ReValueOf.Enumish.valueOf("Leaf")
        }
    }
}
