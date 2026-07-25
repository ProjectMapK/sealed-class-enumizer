package org.wrongwrong.fixtures.mixedorder

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

// 設計00 §6.1 実測形（混在階層）の entries 順序 box テスト
// （docs/テストケース管理.md TC-ORD-004 / TC-ORD-005 / TC-ORD-007 / TC-ORD-020 / TC-ORD-022 / TC-ORD-045 /
// TC-BOX-005）
class MixedOrderTest {
    // TC-ORD-004 / TC-BOX-005: entries = [Box.Bbb, Mmm, S.Aaa, Zzz, aLower]（FQN 序数順）
    @Test
    fun entriesFollowFqnOrdinalOrder() {
        assertEquals(
            listOf("Bbb", "Mmm", "Aaa", "Zzz", "aLower"),
            S.Enumish.entries.map { it.label },
        )
    }

    // TC-ORD-005: 単純名昇順ではない（単純名順なら Aaa が先頭になるが、実際は Box.Bbb が先頭）
    @Test
    fun orderIsNotSimpleNameOrder() {
        val labels = S.Enumish.entries.map { it.label }
        assertNotEquals(labels.sorted(), labels)
        assertNotEquals("Aaa", labels.first())
    }

    // TC-ORD-007: 大小無視順でもない（大小無視なら aLower が先頭になるが、実際は末尾）
    @Test
    fun orderIsCaseSensitive() {
        val labels = S.Enumish.entries.map { it.label }
        assertEquals("aLower", labels.last())
        assertNotEquals(labels.sortedBy { it.lowercase() }, labels)
    }

    // TC-ORD-020: 中間 sealed が無い階層では sealedSubclasses と entries が集合・並びとも一致する
    @Test
    fun entriesMatchSealedSubclassesWhenNoIntermediates() {
        val direct: List<KClass<out S>> = S::class.sealedSubclasses
        val viaEntries: List<KClass<out S>> = S.Enumish.entries.map { it.enumizedClass }
        assertEquals(direct, viaEntries)
    }

    // TC-ORD-022: entries.map{enumizedClass} は entries と同順で List<KClass<out S>> に型付く
    @Test
    fun enumizedClassListFollowsEntriesOrder() {
        val classes: List<KClass<out S>> = S.Enumish.entries.map { it.enumizedClass }
        assertEquals(
            listOf(Box.Bbb::class, Mmm::class, S.Aaa::class, Zzz::class, aLower::class),
            classes,
        )
    }

    // TC-ORD-045: 生成 Enumish / kind は ordinal 相当も Comparable も公開しない。
    // 順序は entries の並びとしてのみ観測でき、indexOf は取れるが永続化には使えない
    @Test
    fun kindsExposeNoComparableAndOrderOnlyViaEntries() {
        val kind: Any = S.Enumish.valueOf("Mmm")
        assertFalse(kind is Comparable<*>)
        assertEquals(1, S.Enumish.entries.indexOf(Mmm))
    }
}
