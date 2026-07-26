package org.wrongwrong.fixtures.reentry

import org.wrongwrong.sealedClassEnumizer.Enumize

// entries の遅延初期化観測（docs/test/ケース01-生成と実行時API.md API-44）。
// 末端 L1 の初期化（init ブロック）は entries の初回アクセスまで走らないことを InitProbe で観測する
@Enumize
sealed interface LazyRoot {
    data object L1 : LazyRoot {
        init {
            InitProbe.events.add("L1")
        }
    }
}
