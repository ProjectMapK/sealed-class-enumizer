package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-36 用の基底（非 final 末端 PolyN + 末端 DupN）
@Enumize
sealed interface NmSubBase {
    abstract class PolyN : NmSubBase {
        companion object
    }

    data object DupN : NmSubBase
}
