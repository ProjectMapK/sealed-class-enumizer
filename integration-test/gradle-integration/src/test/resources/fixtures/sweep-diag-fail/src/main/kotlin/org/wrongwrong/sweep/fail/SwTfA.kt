package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-071 用の階層 A（末端 SwTfCross が属する側）
@Enumize
sealed interface SwTfA {
    data object La : SwTfA
}
