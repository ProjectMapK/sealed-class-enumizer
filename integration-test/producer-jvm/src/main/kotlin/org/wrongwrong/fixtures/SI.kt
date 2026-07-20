package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// 標準形フィクスチャ（docs/テストケース管理.md TC-BOX-001 系・docs/概要.md §1）
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI

    data object Bar : SI
}
