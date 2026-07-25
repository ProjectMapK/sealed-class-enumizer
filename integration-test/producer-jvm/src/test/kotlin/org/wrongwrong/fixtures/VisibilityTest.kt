package org.wrongwrong.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.widerleaf.AutoWide
import org.wrongwrong.fixtures.widerleaf.InternalBase
import org.wrongwrong.fixtures.widerleaf.PublicLeaf
import org.wrongwrong.sealedClassEnumizer.label

// 可視性が混在する階層の box テスト（docs/エッジケースへの対応方針.md §1）
class VisibilityTest {
    // 規則 2 フォールバック構成（internal companion）でも実行時機能は完全に提供される。
    // asEnumish() の静的型は Mixed.Enumish へフォールバックしている（型が付くこと自体が検査）
    @Test
    fun internalCompanionFallsBackToEnumishType() {
        val kind: Mixed.Enumish = Mixed.Half(1).asEnumish()
        assertSame(Mixed.Half.Companion, kind)
        assertEquals(listOf("Full", "Half"), Mixed.Enumish.entries.map { it.label })
    }

    // internal 基底でもモジュール内では全機能が使える（docs/エッジケースへの対応方針.md §1「基底の可視性は制限ではない」）
    @Test
    fun internalBaseWorksInsideModule() {
        assertEquals(listOf("PublicLeaf"), InternalBase.Enumish.entries.map { it.label })
    }

    // TC-VIS-030: 基底より広い末端 + 自動生成 companion（宣言 public・実効 public = 末端）は規則 1 のまま。
    // 具体型（AutoWide.Companion）で受けられること自体が規則 3 非発火の検査
    @Test
    fun autoCompanionOfWiderLeafStaysOnRule1() {
        val kind: AutoWide.Companion = AutoWide(1).asEnumish()
        assertSame(AutoWide.Companion, kind)
        assertEquals("AutoWide", kind.label)
    }

    // 基底より広い可視性の末端（E-2 生成側）: 値・kind API は public 側からも観測できる
    @Test
    fun publicLeafOfInternalBaseProvidesValueApi() {
        val leaf = PublicLeaf()
        assertEquals("PublicLeaf", leaf.label)
        assertSame(PublicLeaf.Companion, leaf.asEnumish())
    }

    // private 基底: 可視範囲（定義ファイル内）では entries / valueOf / 網羅 when がすべて成立する
    @Test
    fun privateBaseWorksInsideItsFile() {
        assertEquals(listOf("Datum", "Hidden"), observePrivateBaseLabels())
        assertEquals("Hidden", observePrivateValueOf("Hidden"))
        assertEquals(listOf("hidden", "datum"), listOf(pickPrivate(0), pickPrivate(1)))
    }
}
