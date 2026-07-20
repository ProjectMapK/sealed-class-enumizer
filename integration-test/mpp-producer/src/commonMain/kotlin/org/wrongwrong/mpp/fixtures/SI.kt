package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// 標準形フィクスチャの MPP 版（docs/テストケース管理.md TC-MPP-001〜005/022・docs/概要.md §1）。
// commonMain に置き、expect / actual でない通常の共通 sealed として全ターゲットで生成される
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI

    data object Bar : SI
}
