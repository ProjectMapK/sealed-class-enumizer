package org.wrongwrong.fixtures.vis.nestbase

import org.wrongwrong.sealedClassEnumizer.Enumize

// 非 sealed クラス内にネストした @Enumize 基底（docs/test/ケース02-可視性.md VIS-04。
// 生成物（Enumish / Companion / EntriesHolder）はネストの内側に生成され多段ネスト生成が成立する）
class Outer {
    @Enumize
    sealed interface SI {
        data object A : SI

        class B(val v: Int) : SI
    }
}
