package org.wrongwrong.fixtures.generic

import org.wrongwrong.sealedClassEnumizer.Enumize

// 型パラメータ付き基底の合成階層（docs/test/ケース01-生成と実行時API.md §8）。
// 生成 Enumish・companion kind は型パラメータを持たず、kind は Generic<*> の分類子
// （型引数は kind の同一性に影響しない。末端のみ generic の形は Holder.kt が担う）
@Enumize
sealed class Generic<T> {
    // 基底・末端の両方が型パラメータを持つ形
    data class Box<T>(val value: T) : Generic<T>()

    class Empty<T> : Generic<T>()

    // 基底のみが型パラメータを持つ形（非 generic 末端）
    class Fixed : Generic<Unit>()
}
