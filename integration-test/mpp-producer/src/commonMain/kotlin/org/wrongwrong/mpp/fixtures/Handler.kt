package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// 末端 fun interface フィクスチャ（V10-c。docs/test/ケース05-境界横断.md XMP-34）。
// 生成 asEnumish は default 実装のため SAM の抽象メソッドは handle の 1 つに保たれる
@Enumize
sealed interface Handler {
    fun interface Fn : Handler {
        fun handle(): String
    }

    data object Direct : Handler
}
