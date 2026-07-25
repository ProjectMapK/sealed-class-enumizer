package org.wrongwrong.fixtures.multienum

import org.wrongwrong.sealedClassEnumizer.Enumize

// 1 階層内に enum 末端が複数（TC-BOX-084）+ enum 定数の toString override が kind と独立（TC-LEAF-032）。
// 各 enum が全体で 1 kind になり、定数（X/Y/P/Q）には展開されない
@Enumize
sealed interface Multi {
    enum class Alpha : Multi {
        X,
        Y,
    }

    enum class Beta : Multi {
        P,
        Q,
    }

    enum class Gamma : Multi {
        // 定数側の toString override は値側の表示にのみ影響し、kind の label / toString には影響しない
        X {
            override fun toString(): String = "x-custom"
        },
        Y,
    }
}
