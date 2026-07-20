package org.wrongwrong.fixtures.emptyhier

import org.wrongwrong.sealedClassEnumizer.Enumize

// 継承者ゼロの中間 sealed（TC-ORD-060）: 中間 None は entries に寄与せず、空展開でも走査は縮退しない
@Enumize
sealed interface WithEmptyMid {
    sealed interface None : WithEmptyMid

    data object A : WithEmptyMid
}
