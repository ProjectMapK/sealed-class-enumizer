package org.wrongwrong.fixtures.si

import org.wrongwrong.sealedClassEnumizer.Enumize

// 標準形フィクスチャ（docs/test/ケース01-生成と実行時API.md §1・docs/概要.md §1）。
// 消費側・Java・binary-swap の共通参照となる最小の 2 末端構成
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI

    data object Bar : SI
}
