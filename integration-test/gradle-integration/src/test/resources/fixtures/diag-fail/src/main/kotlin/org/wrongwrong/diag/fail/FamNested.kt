package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-15: 末端自身への @Enumize → NESTED + 基底 FQN・自己生成 supertype の食い違いで MSM 併発
@Enumize
sealed interface FamNested {
    @Enumize
    data object Leaf : FamNested
}
