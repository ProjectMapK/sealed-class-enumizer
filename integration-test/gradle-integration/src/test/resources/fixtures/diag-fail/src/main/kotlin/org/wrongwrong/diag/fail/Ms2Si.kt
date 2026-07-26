package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-47: 自作 interface 経由（supertypeClosure）の不一致でも MSM
@Enumize
sealed interface Ms2Si : Ms2MyBase {
    data object L2 : Ms2Si
}
