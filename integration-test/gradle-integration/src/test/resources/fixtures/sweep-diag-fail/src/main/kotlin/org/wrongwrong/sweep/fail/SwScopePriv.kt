package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-002 用: private トップレベル基底（同一ファイル内では全 API 成立 = TC-VIS-001 実証済み側）
@Enumize
private sealed interface SwScopePriv {
    data object L : SwScopePriv
}
