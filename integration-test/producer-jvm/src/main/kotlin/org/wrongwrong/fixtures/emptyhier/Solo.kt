package org.wrongwrong.fixtures.emptyhier

import org.wrongwrong.sealedClassEnumizer.Enumize

// 単一末端階層（TC-ORD-047 / TC-BOX-009。順序・走査の下限境界）
@Enumize
sealed interface Solo {
    data object Only : Solo
}
