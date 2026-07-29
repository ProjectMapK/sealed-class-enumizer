package io.github.projectmapk.fixtures.plain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// companion 状態（明示・共存・名前つき）と object 末端の box テスト
// （docs/test/ケース01-生成と実行時API.md §4）
class PlainCompanionTest {
    // docs/test/ケース01-生成と実行時API.md API-23: 明示既定名 companion は kind として流用され
    // （重複生成回避）、ユーザーメンバーと生成メンバー（label / enumizedClass / toString）が共存する
    @Test
    fun explicitCompanionIsReusedWithUserMembers() {
        assertSame(Plain.Simple.Companion, Plain.Simple(1).asEnumish())
        assertSame(Plain.Stocked.Companion, Plain.Stocked(2).asEnumish())
        // ユーザーメンバーは生成後も機能する
        assertEquals(listOf(1, 1), listOf(Plain.Stocked.cfg, Plain.Stocked.make().v))
        // 生成メンバーとの共存
        assertEquals(
            listOf("Stocked", Plain.Stocked::class),
            listOf(Plain.Stocked.Companion.label, Plain.Stocked.Companion.enumizedClass),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-24: 名前つき companion Factory が kind になるが、
    // label は末端単純名で不変（companion 名非依存）。valueOf は companion 名を解決しない
    @Test
    fun namedCompanionIsKindButLabelIsLeafName() {
        assertSame(Plain.Made.Factory, Plain.Made(1).asEnumish())
        assertEquals("Made", Plain.Made.Factory.label)
        assertSame(Plain.Made.Factory, Plain.Enumish.valueOf("Made"))
        assertNull(Plain.Enumish.valueOfOrNull("Factory"))
    }

    // docs/test/ケース03-順序.md ORD-07: 明示 companion（既定名・名前つき Factory）は
    // 順序にも label にも影響しない（キーは末端 ClassId）。非 data object 末端 Marker は
    // 自身が kind で toString = label が生成される
    @Test
    fun companionNamingLeavesOrderAndLabelUntouched() {
        assertEquals(
            listOf("Made", "Marker", "Simple", "Stocked"),
            Plain.Enumish.entries.map { it.label },
        )
        assertSame(Plain.Marker, Plain.Enumish.valueOf("Marker"))
        assertEquals("Marker", Plain.Marker.toString())
    }
}
