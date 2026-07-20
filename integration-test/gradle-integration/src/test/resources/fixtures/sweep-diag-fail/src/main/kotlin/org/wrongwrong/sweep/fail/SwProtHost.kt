package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-009 用: protected ネスト基底（サブクラス文脈での成立は consumer-pure-jvm
// ProtectedBaseSubclassTest = TC-VIS-008 が実証済み）
open class SwProtHost {
    @Enumize
    protected sealed interface P {
        data object L : P
    }
}
