package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（経路内具象実装）でも v1 は一律 MSM
@Enumize
sealed interface Ms8Si : Enumized<Ms8K> {
    data object L8 : Ms8Si
}
