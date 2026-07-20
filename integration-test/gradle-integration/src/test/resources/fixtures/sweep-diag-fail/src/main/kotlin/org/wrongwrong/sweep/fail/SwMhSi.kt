package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-065 用の基底（末端 object Real は SwMhManual の default 実装が返す値を兼ねる）
@Enumize
sealed interface SwMhSi {
    data object Real : SwMhSi
}
