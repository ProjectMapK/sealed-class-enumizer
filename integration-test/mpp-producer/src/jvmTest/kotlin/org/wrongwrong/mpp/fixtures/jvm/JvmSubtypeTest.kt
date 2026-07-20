package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.mpp.fixtures.shape.Shape
import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 非 final 末端（common）のサブタイプを platform ソースセットに置く構成が
// ENUMIZE_CROSS_SOURCE_SET を発火させず kind に吸収されることの box テスト
// （docs/テストケース管理.md TC-MPP-050。コンパイル成立自体が非発火の実証）
class JvmSubtypeTest {
    // 生成 companion の名指しは跨コンパイレーション NG のため valueOf 経由で同一性を観測する
    @Test
    fun platformSourceSetSubtypeIsAbsorbed() {
        val triangle: Shape = JvmTriangle()
        assertSame(Shape.Enumish.valueOf("Polygon"), triangle.asEnumish())
        assertEquals("Polygon", triangle.label)
    }

    // サブタイプを platform に足しても entries は増えない
    @Test
    fun entriesAreUnchangedBySubtype() {
        assertEquals(listOf("Circle", "Custom", "Polygon"), Shape.Enumish.entries.map { it.label })
    }
}
