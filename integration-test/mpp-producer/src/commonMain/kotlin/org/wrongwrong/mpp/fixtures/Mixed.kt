package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// public 基底 + internal companion の MPP 版（docs/エッジケースへの対応方針.md §1.3 規則 2: asEnumish の返り値型が
// Mixed.Enumish へフォールバックする）。規則 2 の判定が全 platform / metadata で一致することを
// 観測する（docs/test/ケース05-境界横断.md XMP-43 の規則 2 側）
@Enumize
sealed interface Mixed {
    class Half(val v: Int) : Mixed {
        internal companion object
    }

    data object Full : Mixed
}
