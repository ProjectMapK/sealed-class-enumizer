package org.wrongwrong.mpp.fixtures.shape

import org.wrongwrong.sealedClassEnumizer.Enumize

// 非 final 末端フィクスチャの MPP 版（V10。docs/概要.md §3・docs/テストケース管理.md
// TC-MPP-045/050/053）。Polygon / Custom は拡張点として開いたまま kind になり、
// サブタイプは別ソースセット（jvmMain の JvmTriangle）・別モジュール（mpp-consumer）に置く
@Enumize
sealed interface Shape {
    data class Circle(val r: Double) : Shape

    abstract class Polygon : Shape

    interface Custom : Shape
}
