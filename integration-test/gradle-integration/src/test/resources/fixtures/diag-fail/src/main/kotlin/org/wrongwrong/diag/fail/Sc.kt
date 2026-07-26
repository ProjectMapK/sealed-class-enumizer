package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-64 用の sealed class 基底（末端種別境界の確認）
@Enumize
sealed class Sc {
    data object Ok : Sc()
}
