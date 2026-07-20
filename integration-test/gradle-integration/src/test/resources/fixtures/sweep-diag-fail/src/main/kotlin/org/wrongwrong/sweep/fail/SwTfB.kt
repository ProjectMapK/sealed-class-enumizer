package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-071 用の階層 B（生成 Enumish を Enumized の型引数として持ち込まれる側）
@Enumize
sealed interface SwTfB {
    data object Lb : SwTfB
}
