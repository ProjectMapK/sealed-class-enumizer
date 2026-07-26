package org.wrongwrong.fixtures.zoo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// 全種別 kind スナップショット・吸収・SAM・boxing（docs/test/ケース01-生成と実行時API.md §2）
class ZooTest {
    // docs/test/ケース01-生成と実行時API.md API-12: 10 種別 12 末端で 1 末端 = 1 kind・
    // label = 単純名・object 系 = 自身 / 他 = companion（自動生成 companion 含む）
    @Test
    fun oneKindPerLeafAcrossAllShapes() {
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
        // object 系は自身が kind
        assertSame(Zoo.ObjectLeaf, Zoo.ObjectLeaf.asEnumish())
        assertSame(Zoo.PlainObject, Zoo.PlainObject.asEnumish())
        // class / enum / value class 系は companion が kind
        assertSame(Zoo.DataLeaf.Companion, Zoo.DataLeaf(1).asEnumish())
        assertSame(Zoo.FinalLeaf.Companion, Zoo.FinalLeaf(1).asEnumish())
        assertSame(Zoo.OpenLeaf.Companion, Zoo.OpenLeaf().asEnumish())
        assertSame(Zoo.AbstractLeaf.Companion, Zoo.Enumish.valueOf("AbstractLeaf"))
        assertSame(Zoo.EnumLeaf.Companion, Zoo.EnumLeaf.ONE.asEnumish())
        assertSame(Zoo.ValueLeaf.Companion, Zoo.ValueLeaf(1).asEnumish())
    }

    // docs/test/ケース01-生成と実行時API.md API-13: 吸収サブタイプ（open 直下・多段・object・
    // interface 実装・無名 object・local class・委譲実装）は kind を新設せず entries 不変・asEnumish 継承
    @Test
    fun subtypesAreAbsorbedIntoLeafKinds() {
        val openKind = Zoo.Enumish.valueOf("OpenLeaf")
        assertSame(openKind, Oval().asEnumish())
        assertSame(openKind, Square().asEnumish())
        assertSame(openKind, Spot.asEnumish())

        // テスト内無名 object（abstract 末端の実体化）
        val anon = object : Zoo.AbstractLeaf() {}
        assertSame(Zoo.AbstractLeaf.Companion, anon.asEnumish())

        val ifaceKind = Zoo.Enumish.valueOf("IfaceLeaf")
        // 第三者実装（default asEnumish の JVM lowering 込み）
        assertSame(ifaceKind, Crafted().asEnumish())
        // テスト内 local class 実装
        class LocalImpl : Zoo.IfaceLeaf
        assertSame(ifaceKind, LocalImpl().asEnumish())
        // 末端 interface への委譲実装
        assertSame(ifaceKind, Veil(Crafted()).asEnumish())

        // 吸収されても entries は末端 12 のまま不変
        assertEquals(12, Zoo.Enumish.entries.size)
    }

    // docs/test/ケース01-生成と実行時API.md API-14: 非 final 末端の enumizedClass は
    // 分類代表（実行時クラスと不一致可）
    @Test
    fun enumizedClassIsRepresentativeForOpenLeaves() {
        val square: Zoo = Square()
        assertEquals(Zoo.OpenLeaf::class, square.asEnumish().enumizedClass)
        assertEquals(Square::class, square::class)
        val crafted: Zoo = Crafted()
        assertEquals(Zoo.IfaceLeaf::class, crafted.asEnumish().enumizedClass)
        assertEquals(Crafted::class, crafted::class)
    }

    // docs/test/ケース01-生成と実行時API.md API-15: 実装者ゼロの interface 末端（Ghost・自動 companion）も
    // entries に掲載される
    @Test
    fun implementorlessInterfaceLeafHasKind() {
        val ghost = Zoo.Enumish.valueOf("Ghost")
        assertSame(Zoo.Ghost.Companion, ghost)
        assertEquals(Zoo.Ghost::class, ghost.enumizedClass)
    }

    // docs/test/ケース01-生成と実行時API.md API-16: 値 when の is 末端枝がサブタイプを被覆する（sealed の地力）
    @Test
    fun valueWhenCoversSubtypesByLeafBranch() {
        fun describe(z: Zoo): String =
            when (z) {
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

        val values =
            listOf<Zoo>(Oval(), Square(), Spot, Crafted(), Veil(Crafted()), Zoo.DataLeaf(1))
        assertEquals(
            listOf("open", "open", "open", "iface", "iface", "data"),
            values.map(::describe),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-17: fun interface 末端の SAM 変換値も同一 kind で、
    // 生成後も SAM が保持される（asEnumish は default 実装）
    @Test
    fun samConversionSharesKind() {
        val explicit = Zoo.FunLeaf { 41 }
        assertEquals(41, explicit.go())
        assertSame(Zoo.FunLeaf.Companion, explicit.asEnumish())
        val auto = Zoo.FunAuto { it * 2 }
        assertEquals(6, auto.run(3))
        assertSame(Zoo.FunAuto.Companion, auto.asEnumish())
    }

    // docs/test/ケース01-生成と実行時API.md API-17: 明示 public companion（FunLeaf / IfaceLeaf）は
    // kind として流用され default asEnumish がそれを返す。FunAuto / Ghost は自動生成側の分岐
    @Test
    fun explicitCompanionInterfaceLeavesResolveKind() {
        assertSame(Zoo.IfaceLeaf.Companion, Zoo.Enumish.valueOf("IfaceLeaf"))
        assertSame(Zoo.FunLeaf.Companion, Zoo.Enumish.valueOf("FunLeaf"))
        assertSame(Zoo.IfaceLeaf.Companion, Crafted().asEnumish())
        assertSame(Zoo.FunAuto.Companion, Zoo.Enumish.valueOf("FunAuto"))
        assertSame(Zoo.Ghost.Companion, Zoo.Enumish.valueOf("Ghost"))
    }

    // docs/test/ケース01-生成と実行時API.md API-18: value class 末端は inline / boxed 双方で
    // kind 同一・label 拡張・asEnumish が安定する
    @Test
    fun valueClassKeepsKindAcrossBoxing() {
        val inline = Zoo.ValueLeaf(1)
        val boxed: Zoo = Zoo.ValueLeaf(2)
        assertSame(Zoo.ValueLeaf.Companion, inline.asEnumish())
        assertSame(inline.asEnumish(), boxed.asEnumish())
        assertEquals(listOf("ValueLeaf", "ValueLeaf"), listOf(inline.label, boxed.label))
    }

    // docs/test/ケース03-順序.md ORD-04: 整列キーは末端 ClassId のみ（種別は無関係）。
    // enum 末端は 1 kind として自 FQN 位置に置かれ（定数非展開）、吸収サブタイプは順序に影響しない
    @Test
    fun entriesOrderIgnoresLeafShape() {
        val labels = Zoo.Enumish.entries.map { it.label }
        assertEquals(labels.sorted(), labels)
        assertEquals(12, labels.size)
    }
}
