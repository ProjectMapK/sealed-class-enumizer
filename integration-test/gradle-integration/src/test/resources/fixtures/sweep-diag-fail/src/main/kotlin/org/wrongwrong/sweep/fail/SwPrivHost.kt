package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-007 用: private ネスト基底（外側クラス本体内では全 API 成立 = TC-VIS-006 側）
class SwPrivHost {
    @Enumize
    private sealed interface N {
        data object L : N
    }

    // 内側からの利用が成立することの対照（生成 API が本体スコープで解決される）
    fun insideCount(): Int = N.Enumish.entries.size
}
