package org.wrongwrong.diag.label

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-098: 末端 enum class の label は enum 全体の単純名（定数名 A/B は衝突判定に関与しない）
@Enumize
sealed interface Lc3Si {
    enum class Dup : Lc3Si { A, B }
}
