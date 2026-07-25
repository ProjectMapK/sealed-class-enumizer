package org.wrongwrong.fixtures.shape

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// 非 final 末端（V10）の box テスト（docs/概要.md §3「拡張点を開いたまま使える」）
class ShapeTest {
    // 末端の数だけ entries に載る（サブタイプでは増えない）。FQN 順 = [Circle, Custom, Polygon]
    @Test
    fun entriesListsLeavesOnly() {
        assertEquals(listOf("Circle", "Custom", "Polygon"), Shape.Enumish.entries.map { it.label })
    }

    // abstract class 末端のサブタイプは asEnumish の実装を継承して Polygon の kind に吸収される
    @Test
    fun subclassOfAbstractLeafIsAbsorbed() {
        val triangle: Shape = Triangle()
        assertSame(Shape.Polygon.Companion, triangle.asEnumish())
        assertEquals("Polygon", triangle.label)
    }

    // interface 末端の実装は default の asEnumish を継承して Custom の kind に吸収される
    @Test
    fun implementationOfInterfaceLeafIsAbsorbed() {
        val custom: Shape = MyCustom()
        assertSame(Shape.Custom.Companion, custom.asEnumish())
        assertEquals("Custom", custom.label)
    }

    // enumizedClass は分類の代表である末端自身を返す（値の実行時クラスとは一致しない場合がある）
    @Test
    fun enumizedClassReturnsRepresentativeLeaf() {
        assertEquals(Shape.Polygon::class, Triangle().asEnumish().enumizedClass)
        assertEquals(Shape.Custom::class, MyCustom().asEnumish().enumizedClass)
    }

    // 値単位の when は is 末端 の枝がサブタイプをすべて覆う
    @Test
    fun valueWhenCoversSubtypesByLeafBranch() {
        val shapes: List<Shape> = listOf(Shape.Circle(1.0), Triangle(), MyCustom())
        val branches = shapes.map { shape ->
            when (shape) {
                is Shape.Circle -> "circle"
                is Shape.Polygon -> "polygon"
                is Shape.Custom -> "custom"
            }
        }
        assertEquals(listOf("circle", "polygon", "custom"), branches)
    }
}
