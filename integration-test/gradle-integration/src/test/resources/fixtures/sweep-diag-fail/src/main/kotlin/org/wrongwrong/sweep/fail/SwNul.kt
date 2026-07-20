package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-MAN-016(1): nullable 型引数の手動 Enumized 宣言。Enumized<out T : Enumish> の非 null 境界に
// 反するため、MISMATCH 照合以前に言語の境界違反（upper bound violated）になる境界
@Enumize
sealed interface SwNul : Enumized<SwNul.Enumish?> {
    data object L : SwNul
}
