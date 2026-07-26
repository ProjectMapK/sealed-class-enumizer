package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// 型パラメータ付き sealed の MPP 版（docs/概要.md §6・docs/test/ケース05-境界横断.md XMP-34）。
// 生成 Enumish は型パラメータを持たず、kind は Generic<*> の分類子である
@Enumize
sealed class Generic<T> {
    data class Box<T>(val value: T) : Generic<T>()

    class Empty<T> : Generic<T>()
}
