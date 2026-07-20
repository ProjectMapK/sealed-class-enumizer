package org.wrongwrong.fixtures.shape

import org.wrongwrong.sealedClassEnumizer.Enumize

// 非 final 末端フィクスチャ（V10。docs/概要.md §3「末端は非 final でもよい」）。
// Polygon / Custom は拡張点として開いたまま kind になる（サブタイプは Triangle.kt / MyCustom.kt）
@Enumize
sealed interface Shape {
    data class Circle(val r: Double) : Shape

    abstract class Polygon : Shape

    interface Custom : Shape
}
