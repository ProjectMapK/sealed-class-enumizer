package org.wrongwrong.consumer.pure

import org.wrongwrong.fixtures.internalleaves.InternalLeaves
import org.wrongwrong.fixtures.internalleaves.PubOb
import org.wrongwrong.fixtures.secret.Sec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// public 基底 + internal / private 末端の跨モジュール観測（docs/エッジケースへの対応方針.md §1.2、
// docs/テストケース管理.md TC-XM-030 / TC-BOX-072 / TC-GAP-015 / TC-VIS-011 / TC-VIS-013 / TC-XM-010 正値側）
class InternalLeavesCrossModuleTest {
    // TC-XM-030 / TC-VIS-011 / TC-GAP-015: 名指し不能な internal 末端（class / enum / object / value class）の
    // kind も entries に載り、label は外部から観測できる（entries の完全性を優先。要素の静的型は public な
    // InternalLeaves.Enumish のため List で受け取れる）
    @Test
    fun internalLeavesAppearInEntriesAcrossModule() {
        val entries: List<InternalLeaves.Enumish> = InternalLeaves.Enumish.entries
        assertEquals(
            listOf("AutoLeaf", "ClsLeaf", "EnLeaf", "ObLeaf", "PubOb", "VvLeaf"),
            entries.map { it.label },
        )
    }

    // TC-BOX-072 / TC-GAP-015: valueOf は internal kind（object / enum / value class）も解決し、
    // enumizedClass 経由で末端の単純名を観測できる（型名の名指しは不可のまま）
    @Test
    fun valueOfResolvesInvisibleKindsAcrossModule() {
        val entries = InternalLeaves.Enumish.entries
        assertSame(entries[3], InternalLeaves.Enumish.valueOf("ObLeaf"))
        assertSame(entries[2], InternalLeaves.Enumish.valueOf("EnLeaf"))
        assertEquals(
            listOf("ObLeaf", "EnLeaf", "VvLeaf"),
            listOf("ObLeaf", "EnLeaf", "VvLeaf").map {
                InternalLeaves.Enumish.valueOf(it).enumizedClass.simpleName ?: ""
            },
        )
    }

    // TC-XM-010（正値側・else 付き）: 可視範囲の外側からは internal kind を名指しできないため、
    // kind-when は可視な枝 + else で書き、不可視 kind は else 枝に落ちる。
    // else 省略が網羅性エラーになる負値側はコンパイル失敗を要するため gradle-integration 担当
    @Test
    fun kindWhenOutsideVisibilityUsesElseBranch() {
        val branches = InternalLeaves.Enumish.entries.map { kind ->
            when (kind) {
                PubOb -> "pub"
                else -> "invisible:${kind.label}"
            }
        }
        assertEquals(
            listOf(
                "invisible:AutoLeaf",
                "invisible:ClsLeaf",
                "invisible:EnLeaf",
                "invisible:ObLeaf",
                "pub",
                "invisible:VvLeaf",
            ),
            branches,
        )
    }

    // TC-VIS-013: 基底内ネストの private 末端も entries / valueOf 経由で外部から観測できる
    @Test
    fun privateNestedLeavesObservableAcrossModule() {
        assertEquals(listOf("Aaa", "Cls", "Mmm", "Zzz"), Sec.Enumish.entries.map { it.label })
        assertEquals("Zzz", Sec.Enumish.valueOf("Zzz").label)
    }
}
