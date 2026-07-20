package org.wrongwrong.fixtures

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

// valueOf / entries の往復整合と照合規則の box テスト
// （docs/テストケース管理.md TC-BOX-010 / TC-BOX-017 / TC-BOX-018 / TC-BOX-085）
class SIRoundTripTest {
    // TC-BOX-010: 全 entries について valueOf(label) が同一 kind へ戻る（往復整合）
    @Test
    fun valueOfRoundTripsAllEntries() {
        SI.Enumish.entries.forEach { kind ->
            assertSame(kind, SI.Enumish.valueOf(kind.label))
            assertSame(kind, SI.Enumish.valueOfOrNull(kind.label))
        }
    }

    // TC-BOX-017: 照合は完全一致のみ（部分一致・大小無視は不一致）
    @Test
    fun valueOfRequiresExactMatch() {
        assertNull(SI.Enumish.valueOfOrNull("Fo"))
        assertNull(SI.Enumish.valueOfOrNull("FOO"))
        assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("foo") }
    }

    // TC-BOX-018: 空文字列・空白も完全一致照合で不一致
    @Test
    fun emptyAndBlankLabelsDoNotMatch() {
        assertNull(SI.Enumish.valueOfOrNull(""))
        assertNull(SI.Enumish.valueOfOrNull(" "))
        assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("") }
    }

    // TC-BOX-085: enumishCompanion 経由で値から valueOf へ到達し往復が閉じる
    @Test
    fun roundTripViaEnumishCompanion() {
        val si: SI = SI.Foo(7)
        assertSame(si.asEnumish(), si.asEnumish().enumishCompanion.valueOf(si.asEnumish().label))
    }
}
