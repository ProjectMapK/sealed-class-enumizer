package org.wrongwrong.fixtures.plain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// final class 末端（明示 companion）・既存 companion との共存・非 data object 末端の box テスト
// （docs/テストケース管理.md TC-LEAF-001 / TC-LEAF-020 / TC-LEAF-024 / TC-LEAF-005 / TC-LEAF-058 /
// TC-LEAF-064）
class PlainTest {
    // TC-LEAF-001: final class 末端。値が別でも kind は同一シングルトン（明示 companion の流用 = TC-LEAF-020）
    @Test
    fun finalClassLeafWithExplicitCompanion() {
        assertSame(Plain.Simple(1).asEnumish(), Plain.Simple(2).asEnumish())
        assertSame(Plain.Simple.Companion, Plain.Simple(1).asEnumish())
        assertEquals("Simple", Plain.Simple.Companion.label)
        assertEquals(Plain.Simple::class, Plain.Simple.Companion.enumizedClass)
    }

    // TC-LEAF-058: 明示実装の無い kind（companion）には toString = label が生成される
    @Test
    fun companionKindToStringReturnsLabel() {
        assertEquals("Simple", Plain.Simple.Companion.toString())
    }

    // TC-LEAF-024: ユーザー既存メンバー（cfg / make）と生成メンバーが共存する
    @Test
    fun userMembersCoexistWithGeneratedMembers() {
        assertEquals(1, Plain.Stocked.cfg)
        assertEquals(1, Plain.Stocked.make().v)
        assertEquals(
            listOf("Stocked", "Stocked"),
            listOf(Plain.Stocked.Companion.label, Plain.Stocked.Companion.toString()),
        )
    }

    // TC-LEAF-005 / TC-LEAF-064 / TC-BOX-042: 非 data の object 末端は kind = 自身で、toString = label
    // が生成される
    @Test
    fun plainObjectLeafIsItsOwnKindWithGeneratedToString() {
        assertSame(Plain.Marker, Plain.Marker.asEnumish())
        assertEquals(
            listOf("Marker", "Marker"),
            listOf(Plain.Marker.label, Plain.Marker.toString()),
        )
        assertEquals(Plain.Marker::class, Plain.Marker.enumizedClass)
    }

    // entries には kind が 1 末端 1 件で載る（FQN 順 = [Marker, Simple, Stocked]）
    @Test
    fun entriesListKindsInFqnOrder() {
        assertEquals(listOf("Marker", "Simple", "Stocked"), Plain.Enumish.entries.map { it.label })
        assertSame(Plain.Marker, Plain.Enumish.entries[0])
        assertSame(Plain.Simple.Companion, Plain.Enumish.entries[1])
    }
}
