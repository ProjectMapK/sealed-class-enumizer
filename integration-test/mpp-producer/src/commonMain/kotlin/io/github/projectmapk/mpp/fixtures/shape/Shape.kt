package io.github.projectmapk.mpp.fixtures.shape

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 非 final 末端フィクスチャの MPP 版（V10。docs/概要.md §3・docs/test/ケース05-境界横断.md
// XMP-34/XMP-42）。Polygon / Custom は拡張点として開いたまま kind になり、
// サブタイプは別ソースセット（jvmMain の JvmTriangle）・別モジュール（mpp-consumer）に置く
@Enumize
sealed interface Shape {
    data class Circle(val r: Double) : Shape

    abstract class Polygon : Shape

    interface Custom : Shape
}
