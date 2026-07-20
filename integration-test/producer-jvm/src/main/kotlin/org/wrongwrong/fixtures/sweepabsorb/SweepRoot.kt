package org.wrongwrong.fixtures.sweepabsorb

import org.wrongwrong.sealedClassEnumizer.Enumize

// 階層外サブタイプの命名フィクスチャ（docs/テストケース管理.md TC-MAN-079）。
// 非 final 末端 Wide のサブタイプに、別末端 Twin と同じ単純名を与えても label 衝突しない
@Enumize
sealed interface SweepRoot {
    data object Twin : SweepRoot

    open class Wide : SweepRoot {
        companion object
    }
}
