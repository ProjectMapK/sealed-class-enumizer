package org.wrongwrong.fixtures

import org.wrongwrong.fixtures.widerleaf.InternalBase
import org.wrongwrong.fixtures.widerleaf.PublicLeaf
import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

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

    // internal 基底でもモジュール内では全機能が使える（エッジ §1「基底の可視性は制限ではない」）
    @Test
    fun internalBaseWorksInsideModule() {
        assertEquals(listOf("PublicLeaf"), InternalBase.Enumish.entries.map { it.label })
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
