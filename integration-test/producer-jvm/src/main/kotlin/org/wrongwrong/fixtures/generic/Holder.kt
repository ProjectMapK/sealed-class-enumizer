package org.wrongwrong.fixtures.generic

import org.wrongwrong.sealedClassEnumizer.Enumize

// 非 generic 基底 + generic 末端の階層（docs/test/ケース01-生成と実行時API.md API-31）。
// 末端のみが型パラメータを持つ形と out 変位注釈の合成
@Enumize
sealed interface Holder {
    data class Cell<out T>(val item: T) : Holder
}
