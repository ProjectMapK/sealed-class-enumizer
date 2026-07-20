package org.wrongwrong.fixtures.casing

import kotlin.test.Test
import kotlin.test.assertEquals

// UTF-16 序数境界の box テスト（docs/テストケース管理.md TC-ORD-008 / TC-ORD-009）
class CasingTest {
    // TC-ORD-008: 'A'(65) < 'a'(97)・第 2 文字 'B'(66) < 'b'(98) の UTF-16 code unit 辞書順。
    // 名前は Cased.kt のコメントのとおり Windows FS の制約で数字サフィックス付き（比較は数字到達前に決着）
    @Test
    fun caseSensitiveUtf16Order() {
        assertEquals(listOf("AB1", "Ab2", "aB3", "ab4"), Cased.Enumish.entries.map { it.label })
    }

    // TC-ORD-009: '1'(49) < 'A'(65) < '_'(95) < 'z'(122) — 数字 < 大文字 < アンダースコア < 小文字
    @Test
    fun digitUpperUnderscoreLowerOrder() {
        assertEquals(listOf("A1", "AA", "A_", "Az"), Glyph.Enumish.entries.map { it.label })
    }
}
