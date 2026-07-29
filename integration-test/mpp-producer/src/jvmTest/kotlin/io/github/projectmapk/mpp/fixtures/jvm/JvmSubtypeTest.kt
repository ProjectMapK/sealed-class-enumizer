package io.github.projectmapk.mpp.fixtures.jvm

import io.github.projectmapk.mpp.fixtures.shape.Shape
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 非 final 末端（common）のサブタイプを platform ソースセットに置く構成が
// sealed のソースセット制約に掛からず kind に吸収されることの box テスト
// （docs/test/ケース05-境界横断.md XMP-42。コンパイル成立自体がその実証）
class JvmSubtypeTest {
    @Test
    fun platformSourceSetSubtypeIsAbsorbed() {
        val triangle: Shape = JvmTriangle()
        assertSame(Shape.Polygon.Companion, triangle.asEnumish())
        assertEquals("Polygon", triangle.label)
    }

    // サブタイプを platform に足しても entries は増えない
    @Test
    fun entriesUnchangedBySubtype() {
        assertEquals(listOf("Circle", "Custom", "Polygon"), Shape.Enumish.entries.map { it.label })
    }
}
