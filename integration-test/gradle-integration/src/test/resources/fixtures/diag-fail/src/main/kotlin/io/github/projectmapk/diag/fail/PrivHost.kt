package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-28 用: private ネスト基底（外側クラス本体内では全 API 成立）
class PrivHost {
    @Enumize
    private sealed interface N {
        data object L : N
    }

    fun insideCount(): Int = N.Enumish.entries.size
}
