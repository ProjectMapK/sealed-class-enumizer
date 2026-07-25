package org.wrongwrong.fixtures.casing

import org.wrongwrong.sealedClassEnumizer.Enumize

// 数字・アンダースコア・英字の UTF-16 序数境界（TC-ORD-009）: '1'(49) < 'A'(65) < '_'(95) < 'z'(122)
@Enumize
sealed interface Glyph {
    data object Az : Glyph

    @Suppress("ClassName") data object A_ : Glyph

    data object AA : Glyph

    data object A1 : Glyph
}
