package org.wrongwrong.diag.kindaccess

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-089: 基底内ネスト末端の private companion → ENUMIZE_KIND_NOT_ACCESSIBLE（対象は kind の可視性）
@Enumize
sealed interface KnaNested {
    class Leaf : KnaNested {
        private companion object
    }
}
