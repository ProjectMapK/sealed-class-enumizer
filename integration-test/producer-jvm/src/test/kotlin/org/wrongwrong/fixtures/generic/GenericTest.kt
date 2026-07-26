package org.wrongwrong.fixtures.generic

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// 型パラメータの box テスト（docs/test/ケース01-生成と実行時API.md §8）
class GenericTest {
    // docs/test/ケース01-生成と実行時API.md API-31: 生成 Enumish・companion kind は無型パラで、
    // 型引数は kind 同一性に無関係（基底のみ / 末端のみ / 両方・out 変位注釈末端の各構成）
    @Test
    fun typeArgumentsDoNotAffectKind() {
        // 基底・末端の両方が型パラメータを持つ形
        assertSame(Generic.Box(1).asEnumish(), Generic.Box("text").asEnumish())
        assertSame(Generic.Box.Companion, Generic.Box(1).asEnumish())
        assertSame(Generic.Empty<Int>().asEnumish(), Generic.Empty<String>().asEnumish())
        // 基底のみ（非 generic 末端）
        assertSame(Generic.Fixed.Companion, Generic.Fixed().asEnumish())
        // 末端のみ + out 変位注釈
        assertSame(Holder.Cell(1).asEnumish(), Holder.Cell("x").asEnumish())
        assertSame(Holder.Cell.Companion, Holder.Cell(1).asEnumish())
        assertEquals(listOf("Box", "Empty", "Fixed"), Generic.Enumish.entries.map { it.label })
        assertEquals(listOf("Cell"), Holder.Enumish.entries.map { it.label })
    }

    // docs/test/ケース01-生成と実行時API.md API-32: enumizedClass は star projection の末端 KClass で、
    // 基底型変数から label 拡張が成立する
    @Test
    fun enumizedClassIsStarProjected() {
        val classes: List<KClass<out Generic<*>>> = Generic.Enumish.entries.map { it.enumizedClass }
        assertEquals(
            listOf(Generic.Box::class, Generic.Empty::class, Generic.Fixed::class),
            classes,
        )
        val generic: Generic<Int> = Generic.Box(1)
        assertEquals("Box", generic.label)
    }
}
