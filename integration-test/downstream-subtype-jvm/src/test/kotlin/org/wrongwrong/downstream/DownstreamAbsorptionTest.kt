package org.wrongwrong.downstream

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.zoo.Zoo
import org.wrongwrong.sealedClassEnumizer.label

// 下流モジュール（プラグイン未適用）でのサブタイプ定義と kind 吸収の box 検証
// （docs/test/ケース05-境界横断.md XMP-18 / XMP-19 / XMP-20）
class DownstreamAbsorptionTest {
    // docs/test/ケース05-境界横断.md XMP-18: abstract 末端の別 module サブタイプは asEnumish を
    // AbstractLeaf から継承して AbstractLeaf の kind に吸収される
    // （プラグイン未適用でも成立 = 生成物の帰属は producer 側に閉じる）
    @Test
    fun subtypeIsAbsorbedIntoLeafKind() {
        val triangle: Zoo = Triangle()
        assertSame(Zoo.AbstractLeaf.Companion, triangle.asEnumish())
        assertEquals("AbstractLeaf", triangle.label)
    }

    // docs/test/ケース05-境界横断.md XMP-18: interface 末端の実装は default の asEnumish を継承して
    // IfaceLeaf の kind（明示 public companion）に吸収される
    @Test
    fun interfaceImplementationIsAbsorbed() {
        val custom: Zoo = MyCustom()
        assertSame(Zoo.IfaceLeaf.Companion, custom.asEnumish())
        assertEquals("IfaceLeaf", custom.label)
    }

    // docs/test/ケース05-境界横断.md XMP-18: fun interface 末端の SAM ラムダ実装も新 kind を作らず
    // FunLeaf の kind に吸収される（asEnumish は default 実装が埋めるため SAM は go 1 つに保たれる）
    @Test
    fun samLambdaIsAbsorbed() {
        val leaf: Zoo.FunLeaf = Zoo.FunLeaf { 41 }
        assertEquals(41, leaf.go())
        val value: Zoo = leaf
        assertSame(Zoo.FunLeaf.Companion, value.asEnumish())
        assertEquals("FunLeaf", value.label)
    }

    // docs/test/ケース05-境界横断.md XMP-18: 下流でサブタイプ（Triangle / MyCustom / SAM ラムダ）を
    // 定義しても entries は末端 12 のまま集合・並びとも不変
    // （entries は階層内の末端で決まり階層外サブタイプに非依存）
    @Test
    fun entriesSetAndOrderUnaffected() {
        assertEquals(
            listOf(
                "AbstractLeaf",
                "DataLeaf",
                "EnumLeaf",
                "FinalLeaf",
                "FunAuto",
                "FunLeaf",
                "Ghost",
                "IfaceLeaf",
                "ObjectLeaf",
                "OpenLeaf",
                "PlainObject",
                "ValueLeaf",
            ),
            Zoo.Enumish.entries.map { it.label },
        )
    }

    // docs/test/ケース05-境界横断.md XMP-19: enumizedClass は分類の代表（末端自身）を返し、
    // 値の実行時クラス（Triangle::class）とは一致しない
    @Test
    fun enumizedClassReturnsRepresentative() {
        val kindClass = Triangle().asEnumish().enumizedClass
        assertEquals(Zoo.AbstractLeaf::class, kindClass)
        assertNotEquals<KClass<*>>(Triangle::class, kindClass)
    }

    // docs/test/ケース05-境界横断.md XMP-20: 値単位 when は is 末端枝が下流サブタイプをすべて覆い、
    // else 無しで網羅する（分類の粒度を固定したまま実装を開けておける設計の跨 module 成立）
    @Test
    fun valueWhenCoversDownstreamSubtypes() {
        val values: List<Zoo> = listOf(Triangle(), MyCustom(), Zoo.FunLeaf { 1 }, Zoo.DataLeaf(1))
        val branches = values.map { zoo ->
            when (zoo) {
                is Zoo.DataLeaf -> "data"
                Zoo.ObjectLeaf -> "dataobj"
                Zoo.PlainObject -> "obj"
                is Zoo.FinalLeaf -> "final"
                is Zoo.OpenLeaf -> "open"
                is Zoo.AbstractLeaf -> "abstract"
                is Zoo.IfaceLeaf -> "iface"
                is Zoo.FunLeaf -> "fun"
                is Zoo.FunAuto -> "funauto"
                is Zoo.Ghost -> "ghost"
                is Zoo.EnumLeaf -> "enum"
                is Zoo.ValueLeaf -> "value"
            }
        }
        assertEquals(listOf("abstract", "iface", "fun", "data"), branches)
    }
}
