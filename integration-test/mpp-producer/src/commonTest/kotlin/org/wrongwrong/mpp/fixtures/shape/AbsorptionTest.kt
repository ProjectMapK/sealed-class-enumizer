package org.wrongwrong.mpp.fixtures.shape

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// 非 final 末端のサブタイプ吸収が全ターゲットで成立することの box テスト
// （docs/test/ケース05-境界横断.md XMP-34・V10）。サブタイプはこのテストクラスに
// ネスト定義する（サブタイプの定義位置は階層外なら任意でよい）
class AbsorptionTest {
    private class CtTriangle : Shape.Polygon()

    private object CtCustom : Shape.Custom

    // entries は末端の数だけ（サブタイプでは増えない）。FQN 順 = [Circle, Custom, Polygon]
    @Test
    fun entriesListLeavesOnly() {
        assertEquals(listOf("Circle", "Custom", "Polygon"), Shape.Enumish.entries.map { it.label })
    }

    // abstract class 末端のサブタイプは asEnumish を継承して kind に吸収される
    @Test
    fun subclassOfAbstractLeafIsAbsorbed() {
        val triangle: Shape = CtTriangle()
        assertSame(Shape.Polygon.Companion, triangle.asEnumish())
        assertEquals("Polygon", triangle.label)
    }

    // interface 末端の default asEnumish 実装が全ターゲットで動作する（V10-b）
    @Test
    fun interfaceLeafDefaultAsEnumishWorks() {
        val custom: Shape = CtCustom
        assertSame(Shape.Custom.Companion, custom.asEnumish())
        assertEquals("Custom", custom.label)
    }

    // enumizedClass は値の実行時クラスではなく分類の代表（末端自身）を返す
    @Test
    fun enumizedClassReturnsRepresentativeLeaf() {
        val triangle: Shape = CtTriangle()
        val custom: Shape = CtCustom
        assertEquals(
            listOf(Shape.Polygon::class, Shape.Custom::class),
            listOf(triangle.asEnumish().enumizedClass, custom.asEnumish().enumizedClass),
        )
    }
}
