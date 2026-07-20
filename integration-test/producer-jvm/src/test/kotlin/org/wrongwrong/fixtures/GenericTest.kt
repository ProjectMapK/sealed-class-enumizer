package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 型パラメータ付き sealed の box テスト（docs/概要.md §6）
class GenericTest {
    @Test
    fun entriesIgnoreTypeArguments() {
        assertEquals(listOf("Box", "Empty"), Generic.Enumish.entries.map { it.label })
    }

    // 型引数は kind の同一性に影響しない
    @Test
    fun kindIsSharedAcrossTypeArguments() {
        assertSame(Generic.Box(1).asEnumish(), Generic.Box("text").asEnumish())
    }

    @Test
    fun enumizedClassIsStarProjectedLeaf() {
        assertEquals(Generic.Box::class, Generic.Box(1).asEnumish().enumizedClass)
    }

    @Test
    fun labelExtensionWorksThroughBaseType() {
        val generic: Generic<Int> = Generic.Empty()
        assertEquals("Empty", generic.label)
    }

    // TC-LEAF-050: 型パラメータ付き末端の companion（自動生成）は型パラメータを持たず、kind は単一
    @Test
    fun companionKindHasNoTypeParameters() {
        assertSame(Generic.Box.Companion, Generic.Box(1).asEnumish())
        assertSame(Generic.Box.Companion, Generic.Enumish.valueOf("Box"))
    }
}
