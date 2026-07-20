package org.wrongwrong.fixtures.valueclass

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// value class 末端の boxing 挙動（docs/エッジケースへの対応方針.md テスト項目のメモ）
class ValueClassTest {
    @Test
    fun valueClassLeafHasKind() {
        assertEquals(listOf("Wrapped"), Valued.Enumish.entries.map { it.label })
    }

    // インライン表現・boxed 表現のどちらから呼んでも kind は同一シングルトン
    @Test
    fun asEnumishIsStableAcrossBoxing() {
        val inline = Wrapped(1)
        val boxed: Valued = Wrapped(2)
        assertSame(inline.asEnumish(), boxed.asEnumish())
        assertSame(Wrapped.Companion, boxed.asEnumish())
    }

    @Test
    fun labelExtensionWorksOnBoxedValue() {
        val boxed: Valued = Wrapped(3)
        assertEquals("Wrapped", boxed.label)
    }

    // TC-LEAF-053: value class 末端の kind の enumizedClass / toString（値側の合成 toString とは別管轄）
    @Test
    fun kindApiOfValueClassLeaf() {
        assertEquals(Wrapped::class, Wrapped.Companion.enumizedClass)
        assertEquals(listOf("Wrapped", "Wrapped(v=1)"), listOf(Wrapped.Companion.toString(), Wrapped(1).toString()))
    }
}
