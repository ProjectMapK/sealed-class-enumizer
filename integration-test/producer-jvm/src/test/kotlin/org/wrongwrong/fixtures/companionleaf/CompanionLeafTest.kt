package org.wrongwrong.fixtures.companionleaf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 階層外クラスの companion が単独で末端になる許容構成の box テスト
// （docs/テストケース管理.md TC-LEAF-046 / TC-BOX-023 / TC-ORD-057 / TC-ORD-058）
class CompanionLeafTest {
    // TC-LEAF-046: 単独 companion 末端は許容され、kind = companion 自身・label = 宣言名（既定 "Companion"）
    @Test
    fun standaloneCompanionLeafIsAllowed() {
        assertSame(Host.Companion, Token.Enumish.valueOf("Companion"))
        assertEquals("Companion", Host.Companion.label)
    }

    // TC-BOX-023: 名前つき companion 自身が末端の場合に限り label = 宣言名
    @Test
    fun namedCompanionLeafUsesDeclaredNameAsLabel() {
        assertSame(WithNamed.Named, Token.Enumish.valueOf("Named"))
        assertEquals("Named", WithNamed.Named.label)
    }

    // TC-ORD-057: 末端 ClassId 順。p.Host.Companion は '.'(46) < 'A'(65) のため p.HostA より先行する
    @Test
    fun companionLeafSortsBeforeSiblingWithSharedPrefix() {
        assertEquals(
            listOf("Aaa", "Companion", "HostA", "Named"),
            Token.Enumish.entries.map { it.label },
        )
    }

    // TC-ORD-058: companion 自身が末端の場合、宣言名（Zzz）は label と末端 ClassId の両方に効く。
    // 順序は末端 ClassId の FQN 序数順であり、p.Holder2.Zzz は共通接頭辞 "Holder2" の後
    // '.'(46) < 'A'(65) の比較で p.Holder2A より先行する（宣言名の 'Z' は比較位置に現れない。
    // ※ docs/テストケース管理.md の当該行は 'Z'90 > 'A'65 で後になるとするが、これは比較位置の算術誤りであり、
    //    docs/概要.md §5 の FQN 序数順に従うとこの並びになる）
    @Test
    fun namedCompanionLeafOrderFollowsFqnOfDeclaredName() {
        assertEquals(listOf("Zzz", "Holder2A"), Badge.Enumish.entries.map { it.label })
        assertSame(Holder2.Zzz, Badge.Enumish.entries[0])
    }
}
