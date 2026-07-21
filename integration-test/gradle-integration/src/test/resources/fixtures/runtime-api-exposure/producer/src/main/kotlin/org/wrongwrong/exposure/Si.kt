package org.wrongwrong.exposure

import org.wrongwrong.sealedClassEnumizer.Enumize

// runtime-api 依存露出の検証用の最小階層（companion は Foo に自動生成される）
@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI
    data object Bar : SI
}
