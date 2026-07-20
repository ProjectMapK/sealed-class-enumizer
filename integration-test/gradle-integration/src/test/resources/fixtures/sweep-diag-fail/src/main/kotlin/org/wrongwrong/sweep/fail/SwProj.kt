package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-MAN-016(2): 使用側 out 射影つきの手動 Enumized 宣言。supertype の直接型引数に射影は
// 言語上許されない（projection in immediate argument）ため、照合以前に言語エラーになる境界
@Enumize
sealed interface SwProj : Enumized<out SwProj.Enumish> {
    data object L : SwProj
}
