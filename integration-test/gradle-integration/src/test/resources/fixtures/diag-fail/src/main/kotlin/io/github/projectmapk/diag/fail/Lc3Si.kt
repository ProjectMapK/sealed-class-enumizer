package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-34: enum 末端の label は enum 全体の単純名（定数名は非関与）
@Enumize
sealed interface Lc3Si {
    enum class Dup : Lc3Si { A, B }
}
