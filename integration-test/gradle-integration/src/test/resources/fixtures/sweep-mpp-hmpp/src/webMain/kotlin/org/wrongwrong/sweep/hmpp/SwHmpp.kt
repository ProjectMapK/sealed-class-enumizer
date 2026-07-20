package org.wrongwrong.sweep.hmpp

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MPP-051 用の基底（中間ソースセット webMain に配置）
@Enumize
sealed interface SwHmpp {
    data object W : SwHmpp
}
