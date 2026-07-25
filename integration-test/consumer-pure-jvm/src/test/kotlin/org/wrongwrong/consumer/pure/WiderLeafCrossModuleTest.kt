package org.wrongwrong.consumer.pure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.widerleaf.AutoWide
import org.wrongwrong.fixtures.widerleaf.PublicLeaf
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.label

// 基底より広い末端（internal 基底 + public class 末端）の外部観測面 = E-2
// （docs/エッジケースへの対応方針.md §1.2・§4、docs/テストケース管理.md TC-XM-031 / TC-VIS-015 /
//  TC-VIS-040 / TC-VIS-041 / TC-VIS-051 / TC-VIS-052 / TC-VIS-053 / TC-LEAF-087 / TC-BOX-071）。
// 基底より広い末端は class 末端でのみ言語上成立するため（interface 末端は TC-GAP-014 NG）、
// interface 末端を前提とする行は class 末端 PublicLeaf ベースへ読み替えている。
// 階層 API（InternalBase.Enumish / entries / valueOf の名指し）が internal で参照不能なことは
// コンパイル失敗を要するため gradle-integration 担当
class WiderLeafCrossModuleTest {
    // TC-VIS-053 / TC-LEAF-087: 明示 public companion（eff(C) = eff(L) = public → 規則 1）のため、
    // 基底不可視の位置でも asEnumish は具体型 PublicLeaf.Companion で返る
    @Test
    fun asEnumishReturnsConcreteCompanionOutsideBaseVisibility() {
        val kind: PublicLeaf.Companion = PublicLeaf().asEnumish()
        assertSame(PublicLeaf.Companion, kind)
    }

    // TC-XM-031 / TC-VIS-015 / TC-VIS-040 / TC-VIS-041: 値・kind API（asEnumish / label / toString /
    // enumizedClass）は末端・companion の public 可視性に従い、基底が見えない位置からも利用できる
    @Test
    fun valueAndKindApiWorkWhereBaseIsInvisible() {
        val kind = PublicLeaf().asEnumish()
        assertEquals(listOf("PublicLeaf", "PublicLeaf"), listOf(kind.label, kind.toString()))
        assertEquals(PublicLeaf::class, kind.enumizedClass)
    }

    // TC-VIS-051: 不可視 supertype（internal InternalBase.Enumish）から継承した public メンバーの解決は
    // 「成立する範囲のみ提供」（docs/エッジケースへの対応方針.md §4 E-2）となる実測:
    // - 成立: label / enumizedClass（override が companion 自身に生成されるため直接解決できる）と、
    //   公開基底 Enumish（runtime-api）への upcast（不可視の中間型を跨げる）。upcast 後は
    //   enumishCompanion 経由で実行時に階層の entries へ到達できる（階層 API の「名指し」は internal のまま）
    // - 不成立: 具体型 PublicLeaf.Companion からの enumishCompanion 直接参照。共変 override の返り値型が
    //   internal な InternalBase.Enumish.Companion のため、以下はコンパイルエラーになる
    //   （e: Cannot access 'interface InternalBase : Any, Enumized<InternalBase.Enumish>': it is
    // internal in file.）:
    //
    //     val direct = PublicLeaf.Companion.enumishCompanion
    //     assertEquals(listOf("PublicLeaf"), direct.entries.map { it.label })
    @Test
    fun inheritedMembersResolveViaPublicBaseAcrossInvisibleSupertype() {
        val asBase: Enumish = PublicLeaf().asEnumish()
        assertEquals("PublicLeaf", asBase.label)
        val reached: List<Enumish> = asBase.enumishCompanion.entries
        assertEquals(listOf("PublicLeaf"), reached.map { it.label })
    }

    // TC-VIS-052: label 拡張（Enumized<T>.label）の T 推論が不可視の InternalBase.Enumish を要する形と、
    // 常に成立する asEnumish().label 経由の対比（E-2 の実測固定）
    @Test
    fun labelExtensionOnWiderLeafValue() {
        val leaf = PublicLeaf()
        assertEquals("PublicLeaf", leaf.asEnumish().label)
        assertEquals("PublicLeaf", leaf.label)
    }

    // TC-BOX-071: 自動生成 companion（宣言 public・実効 public = 末端）の広い末端も規則 1 のまま
    // 外部から具体型で利用できる（KIND_TYPE_NOT_DENOTABLE は発火しない構成）
    @Test
    fun autoCompanionWiderLeafWorksAcrossModule() {
        val kind: AutoWide.Companion = AutoWide(1).asEnumish()
        assertSame(AutoWide.Companion, kind)
        assertEquals("AutoWide", kind.label)
    }
}
