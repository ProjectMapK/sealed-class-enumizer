package org.wrongwrong.fixtures.outerbase

import org.wrongwrong.sealedClassEnumizer.Enumize

// class 内にネストした基底への多段ネスト生成（TC-VIS-005 / TC-BOX-078）。
// 生成物（Enumish / Companion / EntriesHolder）はネストの内側に生成され、実効可視性は min(Outer, SI)
class Outer {
    @Enumize
    sealed interface SI {
        data object A : SI

        class B(val v: Int) : SI
    }
}
