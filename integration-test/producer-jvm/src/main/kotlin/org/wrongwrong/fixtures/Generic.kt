package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// 型パラメータ付き sealed（docs/概要.md §6）: 生成 Enumish は型パラメータを持たず、
// kind は Generic<*> の分類子である（型引数は kind の同一性に影響しない）
@Enumize
sealed class Generic<T> {
    data class Box<T>(val value: T) : Generic<T>()

    class Empty<T> : Generic<T>()
}
