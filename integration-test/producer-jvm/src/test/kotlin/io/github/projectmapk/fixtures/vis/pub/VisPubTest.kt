package io.github.projectmapk.fixtures.vis.pub

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// vis.pub 合成階層の可視性規則・IR-only アクセサ壁・掲載/順序非影響の box テスト
// （docs/test/ケース02-可視性.md §2〜§4・§6・docs/test/ケース03-順序.md ORD-10）
class VisPubTest {
    // ORD-10 / VIS-08 の期待列（FQN UTF-16 序数。VisRoot.kt のコメントと同一）
    private val orderedLabels =
        listOf(
            "AutoLeaf",
            "ClsLeaf",
            "CmdFall",
            "EnLeaf",
            "FarInternal",
            "FarSecret",
            "Forged",
            "Half",
            "HostedLeaf",
            "MA",
            "NearSecret",
            "ObLeaf",
            "PLeaf",
            "PrivComp",
            "ProtComp",
            "ValFall",
            "NestedPrivComp",
            "NestedSecret",
            "VvLeaf",
        )

    private data class InternalShapeSnapshot(
        val listed: Boolean,
        val objectKindIsItself: Boolean,
        val enumConstantName: String,
        val enumKindLabel: String,
        val valueKindStableAcrossBoxing: Boolean,
        val explicitClassKindResolved: Boolean,
        val autoClassKindResolved: Boolean,
    )

    private data class MembershipSnapshot(val size: Int, val labels: Set<String>)

    private data class Rule2ShapeSnapshot(
        val valueKindStableAcrossBoxing: Boolean,
        val valueKindLabel: String,
        val enumConstantName: String,
        val enumKindLabel: String,
        val enumKindShared: Boolean,
    )

    // docs/test/ケース02-可視性.md VIS-07: internal 末端各種別（object/enum/value/class 明示・class 自動）は
    // entries 掲載・module 内で名指し可・enum は name/label 分離・value は boxing 跨ぎ kind 同一
    @Test
    fun internalLeavesOfAllShapesAreListedAndNameable() {
        val labels = VisRoot.Enumish.entries.map { it.label }
        val boxed: VisRoot = VvLeaf(1)
        val expected =
            InternalShapeSnapshot(
                listed = true,
                objectKindIsItself = true,
                enumConstantName = "A",
                enumKindLabel = "EnLeaf",
                valueKindStableAcrossBoxing = true,
                explicitClassKindResolved = true,
                autoClassKindResolved = true,
            )
        val actual =
            InternalShapeSnapshot(
                listed =
                    labels.containsAll(listOf("ObLeaf", "EnLeaf", "VvLeaf", "ClsLeaf", "AutoLeaf")),
                objectKindIsItself = ObLeaf.asEnumish() === ObLeaf,
                enumConstantName = EnLeaf.A.name,
                enumKindLabel = EnLeaf.A.asEnumish().label,
                valueKindStableAcrossBoxing = boxed.asEnumish() === VvLeaf(1).asEnumish(),
                explicitClassKindResolved = ClsLeaf(1).asEnumish() === ClsLeaf.Companion,
                autoClassKindResolved = AutoLeaf(1).asEnumish() === AutoLeaf.Companion,
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-08: 可視性は entries 掲載に無影響（低可視 kind 込みの全掲載）
    @Test
    fun visibilityDoesNotAffectMembershipOrOrder() {
        val labels = VisRoot.Enumish.entries.map { it.label }
        assertEquals(
            MembershipSnapshot(19, orderedLabels.toSet()),
            MembershipSnapshot(labels.size, labels.toSet()),
        )
    }

    // docs/test/ケース02-可視性.md VIS-12: public 末端 × internal companion は規則 2
    // （返り値型は VisRoot.Enumish への型付き代入で固定）・実行時機能は完全
    @Test
    fun internalCompanionFallsBackToEnumishType() {
        val kind: VisRoot.Enumish = Half(1).asEnumish()
        assertSame(Half.Companion, kind)
        assertSame(kind, VisRoot.Enumish.valueOf("Half"))
    }

    // docs/test/ケース02-可視性.md VIS-13: internal 名前付き companion でも規則 2・label は末端単純名で不変
    @Test
    fun internalNamedCompanionFallsBackToEnumishType() {
        val kind: VisRoot.Enumish = Forged(1).asEnumish()
        assertSame(Forged.Factory, kind)
        assertEquals("Forged", kind.label)
    }

    // docs/test/ケース02-可視性.md VIS-14: value class（boxing 跨ぎ kind 同一）・enum class（name/label 分離）
    // × internal companion で規則 2
    @Test
    fun rule2AppliesToValueAndEnumLeaves() {
        val valueKind: VisRoot.Enumish = ValFall(1).asEnumish()
        val boxed: VisRoot = ValFall(2)
        val enumKind: VisRoot.Enumish = CmdFall.GO.asEnumish()
        val expected =
            Rule2ShapeSnapshot(
                valueKindStableAcrossBoxing = true,
                valueKindLabel = "ValFall",
                enumConstantName = "GO",
                enumKindLabel = "CmdFall",
                enumKindShared = true,
            )
        val actual =
            Rule2ShapeSnapshot(
                valueKindStableAcrossBoxing = boxed.asEnumish() === valueKind,
                valueKindLabel = valueKind.label,
                enumConstantName = CmdFall.GO.name,
                enumKindLabel = enumKind.label,
                enumKindShared = CmdFall.STOP.asEnumish() === enumKind,
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-16: 同一ファイル private トップレベル末端・基底ネスト private 末端・
    // 別ファイル internal 末端はいずれも直接参照（アクセサ不要）で entries/valueOf/label に掲載される
    @Test
    fun privateLeavesInFileAndBaseAreListedDirectly() {
        assertEquals(
            listOf("NearSecret", "NestedSecret", "FarInternal"),
            listOf(
                VisRoot.Enumish.valueOf("NearSecret").label,
                VisRoot.Enumish.valueOf("NestedSecret").label,
                VisRoot.Enumish.valueOf("FarInternal").label,
            ),
        )
    }

    // docs/test/ケース02-可視性.md VIS-17: private / protected companion（トップレベル末端・基底ネスト末端の
    // 両配置）は壁の親クラス内アクセサ経由で掲載され、asEnumish の返り値型は規則 2
    @Test
    fun privateAndProtectedCompanionsAreServedViaNestedAccessor() {
        val priv: VisRoot.Enumish = PrivComp(1).asEnumish()
        val prot: VisRoot.Enumish = ProtComp(1).asEnumish()
        val nested: VisRoot.Enumish = VisRoot.NestedPrivComp(1).asEnumish()
        assertSame(VisRoot.Enumish.valueOf("PrivComp"), priv)
        assertSame(VisRoot.Enumish.valueOf("ProtComp"), prot)
        assertSame(VisRoot.Enumish.valueOf("NestedPrivComp"), nested)
    }

    // docs/test/ケース02-可視性.md VIS-18: 別ファイル private トップレベル末端・private 外側クラス内末端は
    // 壁と同一ファイルのトップレベル internal アクセサ関数経由で掲載される
    @Test
    fun filePrivateLeavesAreServedViaTopLevelAccessor() {
        assertEquals(
            listOf("FarSecret", "HostedLeaf"),
            listOf(
                VisRoot.Enumish.valueOf("FarSecret").label,
                VisRoot.Enumish.valueOf("HostedLeaf").label,
            ),
        )
    }

    // docs/test/ケース02-可視性.md VIS-21: internal 中間 sealed は生成・掲載・末端 kind 解決に無影響
    @Test
    fun internalIntermediateDoesNotAffectGeneration() {
        assertSame(MA, MA.asEnumish())
        assertSame(MA, VisRoot.Enumish.valueOf("MA"))
    }

    // docs/test/ケース02-可視性.md VIS-21: private トップレベル中間も同様
    // （中間は生成コードの参照対象外＝アクセサ不要でビルドが成立していること自体が観測）
    @Test
    fun privateIntermediateNeedsNoAccessor() {
        assertEquals("PLeaf", VisRoot.Enumish.valueOf("PLeaf").label)
    }

    // docs/test/ケース03-順序.md ORD-10: private / internal / protected の末端・companion・中間の可視性は
    // 掲載と相対順に非影響（全末端が可視性無関係に FQN 位置）
    @Test
    fun visibilityDoesNotAffectEntriesOrder() {
        assertEquals(orderedLabels, VisRoot.Enumish.entries.map { it.label })
    }
}
