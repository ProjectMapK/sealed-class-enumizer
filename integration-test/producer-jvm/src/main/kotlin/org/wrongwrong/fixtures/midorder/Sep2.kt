package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-ORD-048: 継承者比較 "Sep2.M" vs "M0" は先頭 'S'(83) > 'M'(77) で決着し
// entries = [M0, Zzz48]（'.' と '0' の比較はここでは生じない — その境界は Sep64 が踏む）
@Enumize
sealed interface Sep2 {
    sealed interface M : Sep2 {
        data object Zzz48 : M
    }
}
