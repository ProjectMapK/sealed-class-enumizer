package org.wrongwrong.fixtures.companionleaf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 階層外クラスの companion が単独で末端になる許容構成の box テスト
// （docs/test/ケース01-生成と実行時API.md §5・docs/test/ケース03-順序.md ORD-08）
class CompanionLeafTest {
    // docs/test/ケース01-生成と実行時API.md API-25: 既定名 companion の単独末端は許容され、
    // kind = companion 自身・label = "Companion"
    @Test
    fun defaultNameCompanionLeafIsAllowed() {
        assertSame(Host.Companion, Token.Enumish.valueOf("Companion"))
        assertEquals("Companion", Host.Companion.label)
        assertSame(Host.Companion, Host.Companion.asEnumish())
    }

    // docs/test/ケース01-生成と実行時API.md API-26: 名前つき companion 末端の label = 宣言名
    @Test
    fun namedCompanionLeafUsesDeclaredName() {
        assertEquals(listOf("Named", "Zzz"), listOf(WithNamed.Named.label, Holder2.Zzz.label))
        assertSame(WithNamed.Named, Token.Enumish.valueOf("Named"))
        assertSame(Holder2.Zzz, Token.Enumish.valueOf("Zzz"))
    }

    // docs/test/ケース03-順序.md ORD-08: companion 自身が末端なら宣言名が ClassId と label を決める。
    // 期待列 = [Zzz, Companion, HostA, Named]（'l'(108) < 's'(115) で Holder2.Zzz が先頭・
    // '.'(46) < 'A'(65) で Host.Companion が HostA に先行）
    @Test
    fun companionLeafOrderAndLabelFollowDeclaredName() {
        assertEquals(
            listOf("Zzz", "Companion", "HostA", "Named"),
            Token.Enumish.entries.map { it.label },
        )
        assertSame(Holder2.Zzz, Token.Enumish.entries[0])
    }
}
