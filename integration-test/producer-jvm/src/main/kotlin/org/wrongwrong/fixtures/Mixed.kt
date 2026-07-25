package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// public 基底 + internal companion（docs/エッジケースへの対応方針.md §1.3 規則 2:
// asEnumish の返り値型が Mixed.Enumish へフォールバック）
@Enumize
sealed interface Mixed {
    class Half(val v: Int) : Mixed {
        internal companion object
    }

    data object Full : Mixed
}
