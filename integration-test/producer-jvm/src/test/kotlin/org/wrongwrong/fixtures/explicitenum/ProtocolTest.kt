package org.wrongwrong.fixtures.explicitenum

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// 明示 companion 付き enum 末端の box テスト（docs/テストケース管理.md TC-LEAF-100）
class ProtocolTest {
    // 明示 companion は自動生成されず既存流用され、kind = Verb.Companion になる
    @Test
    fun explicitCompanionIsReusedAsKind() {
        assertSame(Protocol.Verb.Companion, Protocol.Verb.GET.asEnumish())
        assertSame(Protocol.Verb.Companion, Protocol.Enumish.valueOf("Verb"))
    }

    // 3 つの entries 名前空間が併存する: enum 本来の entries / 階層の entries / kind
    @Test
    fun threeEntriesNamespacesCoexist() {
        assertEquals(listOf(Protocol.Verb.GET, Protocol.Verb.POST), Protocol.Verb.entries.toList())
        assertEquals(listOf("Custom", "Verb"), Protocol.Enumish.entries.map { it.label })
    }

    // name（定数名）と label（kind）の併存
    @Test
    fun nameAndLabelCoexist() {
        assertEquals(listOf("GET", "Verb"), listOf(Protocol.Verb.GET.name, Protocol.Verb.GET.label))
    }
}
