package org.wrongwrong.mpp.consumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.mpp.fixtures.shape.Shape
import org.wrongwrong.sealedClassEnumizer.label

// 非 final 末端のサブタイプを別モジュールに置く構成の box テスト
// （docs/テストケース管理.md TC-MPP-045/053・V10。サブタイプ定義に @Enumize は不要）
class ConsumerSubtypeTest {
    private class Tri : Shape.Polygon()

    private object Cust : Shape.Custom

    // TC-MPP-045: 別モジュールのサブタイプは Polygon の kind に吸収され entries は増えない
    @Test
    fun subtypeIsAbsorbedIntoLeafKindAcrossModules() {
        val tri: Shape = Tri()
        assertSame(Shape.Polygon.Companion, tri.asEnumish())
        assertEquals("Polygon", tri.label)
        assertEquals(listOf("Circle", "Custom", "Polygon"), Shape.Enumish.entries.map { it.label })
        assertEquals(Shape.Polygon::class, tri.asEnumish().enumizedClass)
    }

    // TC-MPP-053: interface 末端の default asEnumish 実装が跨モジュール実装でも動作する（V10-b）
    @Test
    fun interfaceLeafDefaultAsEnumishWorksAcrossModules() {
        val cust: Shape = Cust
        assertSame(Shape.Enumish.valueOf("Custom"), cust.asEnumish())
        assertEquals("Custom", cust.label)
    }
}
