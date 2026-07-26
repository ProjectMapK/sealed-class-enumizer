package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-36: enum 定数名（Solo）は判定非関与 → 末端 Solo と衝突しない
@Enumize
sealed interface NmEc {
    enum class Pack : NmEc {
        Solo,
    }

    data object Solo : NmEc
}
