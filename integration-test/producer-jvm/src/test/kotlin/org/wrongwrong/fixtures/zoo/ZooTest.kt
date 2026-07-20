package org.wrongwrong.fixtures.zoo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 全種別混在階層の entries 内容（docs/テストケース管理.md TC-LEAF-011）
class ZooTest {
    // 末端 1 種類につき kind が 1 つずつ・要素集合が末端数と一致・label は単純名・順序は FQN 序数順
    @Test
    fun entriesContainOneKindPerLeafInFqnOrder() {
        assertEquals(
            listOf(
                "AbstractLeaf",
                "DataLeaf",
                "EnumLeaf",
                "FunLeaf",
                "IfaceLeaf",
                "ObjectLeaf",
                "OpenLeaf",
                "PlainObject",
                "ValueLeaf",
            ),
            Zoo.Enumish.entries.map { it.label },
        )
    }

    // object 系末端は自身が kind、それ以外は companion が kind
    @Test
    fun objectLeavesAreTheirOwnKindsOthersUseCompanions() {
        val entries = Zoo.Enumish.entries
        assertSame(Zoo.ObjectLeaf, entries[5])
        assertSame(Zoo.PlainObject, entries[7])
        assertSame(Zoo.DataLeaf.Companion, entries[1])
        assertSame(Zoo.EnumLeaf.Companion, entries[2])
        assertSame(Zoo.ValueLeaf.Companion, entries[8])
    }
}
