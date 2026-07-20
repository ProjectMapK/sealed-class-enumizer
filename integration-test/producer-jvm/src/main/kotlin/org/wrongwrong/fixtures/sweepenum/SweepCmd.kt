package org.wrongwrong.fixtures.sweepenum

import org.wrongwrong.sealedClassEnumizer.Enumize

// enum 定数名と末端単純名の管轄分離フィクスチャ（docs/テストケース管理.md TC-MAN-078）。
// enum 定数 HELP は label ではない（enum 末端の label は enum class 宣言名 "Builtin"）ため、
// 同名の末端 object HELP と ENUMIZE_LABEL_CLASH を起こさない
@Enumize
sealed interface SweepCmd {
    enum class Builtin : SweepCmd {
        HELP,
        VERSION,
    }

    data object HELP : SweepCmd
}
