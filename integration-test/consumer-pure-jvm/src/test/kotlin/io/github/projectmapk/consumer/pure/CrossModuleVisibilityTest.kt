package io.github.projectmapk.consumer.pure

import io.github.projectmapk.fixtures.vis.narrow.AutoWide
import io.github.projectmapk.fixtures.vis.narrow.PublicLeaf
import io.github.projectmapk.fixtures.vis.nestbase.ProtectedHost
import io.github.projectmapk.fixtures.vis.pub.Forged
import io.github.projectmapk.fixtures.vis.pub.Half
import io.github.projectmapk.fixtures.vis.pub.MA
import io.github.projectmapk.fixtures.vis.pub.VisRoot
import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 可視性境界と E-2 外部観測面の跨 module 観測
// （docs/test/ケース05-境界横断.md XMP-13 / XMP-14 / XMP-16 / XMP-17 / XMP-50。
// 3 段規則・壁 3 形など可視性規則の正典はケース02。全 internal 階層の完全不可視 = XMP-15 と
// else 省略・階層 API 参照の言語エラーはコンパイル失敗を要するため gradle-integration 担当）
class CrossModuleVisibilityTest {
    // vis.pub の期待 entries（label 列。VisRoot.kt のコメントおよび VisPubTest と同一）
    private val visRootLabels =
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

    // docs/test/ケース05-境界横断.md XMP-14: 名指し不能な internal / private 末端も含む全 19 kind が
    // entries に載り、label は外部から観測できる（要素の静的型は public な VisRoot.Enumish）
    @Test
    fun internalLeavesAppearInEntries() {
        val entries: List<VisRoot.Enumish> = VisRoot.Enumish.entries
        assertEquals(visRootLabels, entries.map { it.label })
    }

    // docs/test/ケース05-境界横断.md XMP-14: valueOf は internal kind（object / enum / value class /
    // class 明示 / class 自動 / 別ファイル）も解決し、enumizedClass 経由で末端単純名を観測できる
    // （型の名指しは不可のまま）
    @Test
    fun valueOfResolvesInvisibleKinds() {
        val names = listOf("ObLeaf", "EnLeaf", "VvLeaf", "ClsLeaf", "AutoLeaf", "FarInternal")
        assertEquals(
            names,
            names.map { VisRoot.Enumish.valueOf(it).enumizedClass.simpleName ?: "" },
        )
    }

    // docs/test/ケース05-境界横断.md XMP-14: private 末端（基底ネスト・同一ファイル・別ファイル = TL 壁・
    // private 外側クラス内 = TL 壁・private 中間経由）と private companion（ネスト壁）も
    // entries / valueOf 経由で外部から観測できる
    @Test
    fun privateNestedLeavesObservable() {
        val names =
            listOf(
                "NestedSecret",
                "NearSecret",
                "FarSecret",
                "HostedLeaf",
                "PLeaf",
                "NestedPrivComp",
            )
        assertEquals(names, names.map { VisRoot.Enumish.valueOf(it).label })
    }

    // docs/test/ケース05-境界横断.md XMP-13: 可視範囲の外側では internal / private kind を名指しできず、
    // kind-when は可視な枝（public data object の MA）+ else で書き、不可視 kind は else 枝に落ちる。
    // else 省略が言語エラーになる負値側は gradle-integration の
    // CrossModuleNegativeTest#elseIsRequiredForInvisibleKindsAndManualImpls が担う
    @Test
    fun kindWhenOutsideVisibilityUsesElse() {
        val branches =
            VisRoot.Enumish.entries.map { kind ->
                when (kind) {
                    MA -> "visible"
                    else -> "invisible:${kind.label}"
                }
            }
        assertEquals(visRootLabels.map { if (it == "MA") "visible" else "invisible:$it" }, branches)
    }

    // docs/test/ケース05-境界横断.md XMP-16: 規則 2 フォールバック末端（internal companion）の asEnumish は
    // 生成 Enumish 型（VisRoot.Enumish）で返り、Half.Companion は跨 module で名指しできないため
    // kind の突き合わせは entries / valueOf の同一性で行う。label は companion 名でなく末端単純名のまま
    @Test
    fun asEnumishFallsBackToEnumishType() {
        val kind: VisRoot.Enumish = Half(1).asEnumish()
        assertSame(VisRoot.Enumish.valueOf("Half"), kind)
        assertSame(Half(2).asEnumish(), kind)
        assertEquals("Forged", Forged(1).asEnumish().label)
    }

    // docs/test/ケース05-境界横断.md XMP-17: internal 基底 + public class 末端（E-2）では、値・kind API は
    // 末端の可視性に従い基底不可視でも利用できる。明示 public companion は eff(C) = eff(L) = public の
    // 規則 1 で具体型 PublicLeaf.Companion のまま返る。具体型 companion を主語にした when は
    // sealed 網羅が効かず else が要る。
    // NG 固定: 階層 API と基底 when は基底不可視で構成できない
    // （e: Cannot access 'NarrowBase': it is internal in ...）:
    //
    //   NarrowBase.Enumish.entries
    //   val kind: NarrowBase.Enumish = PublicLeaf(1).asEnumish()
    @Test
    fun widerLeafValueAndKindApiWork() {
        val kind: PublicLeaf.Companion = PublicLeaf(1).asEnumish()
        assertSame(PublicLeaf.Companion, kind)
        assertEquals(listOf("PublicLeaf", "PublicLeaf"), listOf(kind.label, kind.toString()))
        assertEquals(PublicLeaf::class, kind.enumizedClass)
        // 主語を公開基底 Enumish（非 sealed・runtime-api）にすると sealed 網羅が効かず else が要る
        val branch =
            when (PublicLeaf(1).asEnumish() as Enumish) {
                PublicLeaf.Companion -> "public-leaf"
                else -> "unreachable"
            }
        assertEquals("public-leaf", branch)
    }

    // docs/test/ケース05-境界横断.md XMP-17: 不可視 supertype から継承した public メンバーの解決は
    // 成立する範囲のみ提供される実測:
    // - 成立: label / enumizedClass（override が companion 自身に生成される）と、公開基底 Enumish
    //   （runtime-api）への upcast。upcast 後は enumishCompanion 経由で実行時に階層の entries へ到達できる
    // - 不成立（NG 固定）: 具体型からの enumishCompanion 直接参照。共変 override の返り値型が internal な
    //   NarrowBase.Enumish.Companion のため次はコンパイルエラーになる
    //   （e: Cannot access 'NarrowBase': it is internal in ...）:
    //
    //     PublicLeaf.Companion.enumishCompanion
    @Test
    fun widerLeafInheritedMembersResolve() {
        val asBase: Enumish = PublicLeaf(1).asEnumish()
        assertEquals("PublicLeaf", asBase.label)
        val reached: List<Enumish> = asBase.enumishCompanion.entries
        assertEquals(
            listOf("AutoWide", "Nested", "NarrowLeaf", "PublicLeaf"),
            reached.map { it.label },
        )
    }

    // docs/test/ケース05-境界横断.md XMP-17: 常に成立する asEnumish().label 経由と、T 推論が不可視の
    // NarrowBase.Enumish を跨ぐ label 拡張の両経路が成立する対比
    @Test
    fun widerLeafLabelExtensionWorks() {
        val leaf = PublicLeaf(1)
        assertEquals(listOf("PublicLeaf", "PublicLeaf"), listOf(leaf.asEnumish().label, leaf.label))
    }

    // docs/test/ケース05-境界横断.md XMP-17: 自動生成 companion（宣言 public・実効可視性は末端追随）の
    // 広い末端も規則 1 のまま具体型で利用できる（KIND_TYPE_NOT_DENOTABLE 非発火の成立形）
    @Test
    fun autoCompanionWiderLeafWorks() {
        val kind: AutoWide.Companion = AutoWide(1).asEnumish()
        assertSame(AutoWide.Companion, kind)
        assertEquals("AutoWide", kind.label)
    }

    // docs/test/ケース05-境界横断.md XMP-50: protected ネスト基底（ProtectedHost.Shielded）の階層 API
    // （entries / valueOf / label）と else 無し kind-when が、跨 module のサブクラス文脈で成立する
    // （生成側の正典はケース02 VIS-03）
    @Test
    fun protectedBaseWorksInSubclassContext() {
        assertEquals(
            ShieldedObservation(
                entryLabels = listOf("Off", "On"),
                valueOfResolvesOn = true,
                offLabel = "Off",
                branches = listOf("off", "on"),
            ),
            ShieldedObserver().observe(),
        )
    }

    // protected の可視範囲（ProtectedHost とそのサブクラス）に入るための観測用サブクラス。
    // else 無し kind-when がコンパイルできること自体が網羅性算入の検査
    private class ShieldedObserver : ProtectedHost() {
        fun observe(): ShieldedObservation {
            val off: Shielded = Shielded.Off(1)
            val branches =
                listOf(off, Shielded.On).map { value ->
                    when (value.asEnumish()) {
                        Shielded.Off.Companion -> "off"
                        Shielded.On -> "on"
                    }
                }
            return ShieldedObservation(
                entryLabels = Shielded.Enumish.entries.map { it.label },
                valueOfResolvesOn = Shielded.Enumish.valueOf("On") === Shielded.On,
                offLabel = off.label,
                branches = branches,
            )
        }
    }

    private data class ShieldedObservation(
        val entryLabels: List<String>,
        val valueOfResolvesOn: Boolean,
        val offLabel: String,
        val branches: List<String>,
    )
}
