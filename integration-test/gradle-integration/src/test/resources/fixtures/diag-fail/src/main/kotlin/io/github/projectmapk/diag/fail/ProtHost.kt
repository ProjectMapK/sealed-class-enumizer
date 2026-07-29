package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-28 用: protected ネスト基底（サブクラス文脈の成立側は XMP-50）
open class ProtHost {
    @Enumize
    protected sealed interface P {
        data object L : P
    }
}
