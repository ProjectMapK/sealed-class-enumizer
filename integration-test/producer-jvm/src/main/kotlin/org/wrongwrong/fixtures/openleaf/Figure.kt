package org.wrongwrong.fixtures.openleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// open class 末端（V10）と実装者ゼロの interface 末端のフィクスチャ
// （TC-LEAF-007 / TC-LEAF-084 / TC-LEAF-080 / TC-BOX-077 / TC-BOX-082。
//  サブタイプは Oval.kt / SpecialRound.kt / Quad.kt / Square.kt）
@Enumize
sealed interface Figure {
    // open class 末端・companion 明示なし（自動生成される。abstract 側の自動生成は shape.Shape.Polygon が担う）
    open class Round(val r: Int) : Figure

    // 実装者ゼロの interface 末端（companion 自動生成）。kind は entries に載る（TC-BOX-077）
    interface Ghost : Figure
}
