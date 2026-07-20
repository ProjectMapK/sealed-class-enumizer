package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

// 型パラメータ付き基底 / 末端の kind と enumizedClass（star projection 相当）が
// 全ターゲットで一致することの box テスト（docs/テストケース管理.md TC-MPP-043）
class GenericTest {
    // 型引数は kind の同一性に影響しない（Box<Int> / Box<String> は同一 kind）。
    // 末端型受け手からの asEnumish() は klib ターゲットで生成 companion が
    // Cannot access class になる NG のため、基底型経由で観測する（docs/修正方針案.md 反映待ち）
    @Test
    fun kindIsSharedAcrossTypeArguments() {
        val intBox: Generic<Int> = Generic.Box(1)
        val stringBox: Generic<String> = Generic.Box("text")
        assertSame(intBox.asEnumish(), stringBox.asEnumish())
        assertEquals(listOf("Box", "Empty"), Generic.Enumish.entries.map { it.label })
    }

    // kind の enumizedClass は末端の class リテラル（KClass<Generic.Box<*>> 相当）
    @Test
    fun enumizedClassIsStarProjectedLeaf() {
        val box: Generic<Int> = Generic.Box(1)
        assertEquals(Generic.Box::class, box.asEnumish().enumizedClass)
    }

    // enumizedRootClass（protected）は失敗メッセージの simpleName 経由で間接観測する
    @Test
    fun rootClassNameAppearsInFailureMessage() {
        val failure = assertFailsWith<IllegalArgumentException> { Generic.Enumish.valueOf("X") }
        assertEquals("No enumish entry with label 'X' in Generic", failure.message)
    }

    // label 拡張は基底型（型引数付き）経由でも動作する
    @Test
    fun labelExtensionWorksThroughParameterizedBaseType() {
        val generic: Generic<Int> = Generic.Empty()
        assertEquals("Empty", generic.label)
    }
}
