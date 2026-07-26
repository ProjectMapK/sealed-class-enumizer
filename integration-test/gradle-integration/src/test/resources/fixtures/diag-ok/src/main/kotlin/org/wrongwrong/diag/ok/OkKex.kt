package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-58: 末端 class の companion による `: Enumish` 明示宣言
// （kind companion 免除の成立側・メンバーは生成が充足）→ MIOH / MSM / MMC 非発火
@Enumize
sealed interface OkKex {
    class Leaf(val v: Int) : OkKex {
        companion object : OkKex.Enumish
    }
}
