package org.wrongwrong.downstream

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.handler.Event
import org.wrongwrong.fixtures.shape.Shape
import org.wrongwrong.sealedClassEnumizer.label

// 下流モジュール（プラグイン未適用）でのサブタイプ定義と kind 吸収の box 検証
// （docs/概要.md §3・§7、docs/テストケース管理.md TC-XM-016〜018 / TC-XM-020 / TC-XM-052 /
//  TC-LEAF-039 / TC-ORD-055 / TC-BOX-081）
class DownstreamAbsorptionTest {
    // TC-XM-016 / TC-BOX-081: 別モジュールのサブタイプは asEnumish を Polygon から継承して
    // Polygon の kind に吸収される（プラグイン未適用でも成立 = 生成物の帰属は producer 側に閉じる）
    @Test
    fun subtypeIsAbsorbedIntoAbstractLeafKind() {
        val triangle: Shape = Triangle()
        assertSame(Shape.Polygon.Companion, triangle.asEnumish())
        assertEquals("Polygon", triangle.label)
    }

    // TC-XM-017: interface 末端の実装は default の asEnumish を継承して吸収される（V10-b）
    @Test
    fun interfaceImplementationIsAbsorbedIntoLeafKind() {
        val custom: Shape = MyCustom()
        assertSame(Shape.Custom.Companion, custom.asEnumish())
        assertEquals("Custom", custom.label)
    }

    // TC-LEAF-039 / TC-ORD-055: 下流でサブタイプ（Triangle / MyCustom）を定義しても
    // entries は集合・並びとも不変（entries は階層内の末端で決まり階層外サブタイプに非依存）
    @Test
    fun entriesSetAndOrderUnaffectedByDownstreamSubtypes() {
        assertEquals(listOf("Circle", "Custom", "Polygon"), Shape.Enumish.entries.map { it.label })
    }

    // TC-XM-018: enumizedClass は分類の代表（末端自身）を返し、値の実行時クラス（Triangle::class）とは
    // 一致しない（docs/概要.md §2 の明記どおり）
    @Test
    fun enumizedClassReturnsRepresentativeNotRuntimeClass() {
        val kindClass = Triangle().asEnumish().enumizedClass
        assertEquals(Shape.Polygon::class, kindClass)
        assertNotEquals<KClass<*>>(Triangle::class, kindClass)
    }

    // TC-XM-020: 値単位 when は is 末端 の枝が下流サブタイプをすべて覆い、else 無しで網羅する
    // （分類の粒度を固定したまま実装を開けておける設計の跨モジュール成立）
    @Test
    fun valueWhenLeafBranchesCoverDownstreamSubtypes() {
        val shapes: List<Shape> = listOf(Shape.Circle(1.0), Triangle(), MyCustom())
        val branches = shapes.map { shape ->
            when (shape) {
                is Shape.Circle -> "circle"
                is Shape.Custom -> "custom"
                is Shape.Polygon -> "polygon"
            }
        }
        assertEquals(listOf("circle", "polygon", "custom"), branches)
    }

    // TC-XM-052: fun interface 末端の SAM ラムダ実装も新 kind を作らず Handler の kind に吸収される
    // （asEnumish は default 実装が埋めるため SAM は handle 1 つに保たれる = V10-c）
    @Test
    fun samLambdaIsAbsorbedIntoFunInterfaceLeafKind() {
        val handler: Event.Handler = Event.Handler { it + 1 }
        assertEquals(42, handler.handle(41))
        val value: Event = handler
        assertSame(Event.Handler.Companion, value.asEnumish())
        assertEquals("Handler", value.label)
        assertEquals(Event.Handler::class, value.asEnumish().enumizedClass)
        assertEquals(listOf("Handler", "Listener"), Event.Enumish.entries.map { it.label })
    }
}
