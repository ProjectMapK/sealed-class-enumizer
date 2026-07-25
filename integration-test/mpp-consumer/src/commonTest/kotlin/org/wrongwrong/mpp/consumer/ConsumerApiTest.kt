package org.wrongwrong.mpp.consumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.wrongwrong.mpp.fixtures.EmptyRoot
import org.wrongwrong.mpp.fixtures.SI
import org.wrongwrong.sealedClassEnumizer.label

// 跨モジュール × MPP: プラグイン未適用モジュールの共通コードから mpp-producer の生成 API を
// 参照して各 platform で動作することの box テスト（docs/テストケース管理.md mpp-consumer 行・
// TC-MPP-015/018 の跨モジュール面・TC-GAP-018）
class ConsumerApiTest {
    // 生成 API 一式が共通メタデータ / klib 経由で解決・動作する
    @Test
    fun generatedApiWorksAcrossModules() {
        val entries: List<SI.Enumish> = SI.Enumish.entries
        assertEquals(listOf("Bar", "Foo"), entries.map { it.label })
        val si: SI = SI.Foo(1)
        assertSame(entries[1], si.asEnumish())
        assertSame(si.asEnumish(), SI.Enumish.valueOf("Foo"))
        assertNull(SI.Enumish.valueOfOrNull("X"))
        assertEquals("Foo", si.label)
        assertEquals("Foo", si.asEnumish().enumizedClass.simpleName)
        assertSame(SI.Enumish.entries, si.asEnumish().enumishCompanion.entries)
    }

    // commonMain の利用関数（consumer の metadata コンパイルで解決される = V5 の跨モジュール面）
    @Test
    fun commonMainUsageFunctionsWork() {
        assertEquals(listOf("Bar", "Foo"), consumerLabels())
    }

    // 失敗メッセージは跨モジュールでも全プラットフォーム同一文言（TC-MPP-013 の跨モジュール面）
    @Test
    fun valueOfFailureMessageIsUniform() {
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("NoSuch") }
        assertEquals("No enumish entry with label 'NoSuch' in SI", failure.message)
    }

    // TC-GAP-018: 空階層の跨モジュール / MPP 観測（entries = []・valueOf は常に失敗）
    @Test
    fun emptyHierarchyIsObservableAcrossModules() {
        assertEquals(emptyList(), EmptyRoot.Enumish.entries)
        val failure = assertFailsWith<IllegalArgumentException> { EmptyRoot.Enumish.valueOf("Any") }
        assertEquals("No enumish entry with label 'Any' in EmptyRoot", failure.message)
    }

    // 末端 class の kind を companion 名で参照する（docs/概要.md §1）。共通ソース由来の
    // 生成 companion がメタデータ / klib のネスト分類子として跨モジュールで解決できる
    @Test
    fun generatedCompanionIsNameableAcrossModules() {
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        assertEquals(
            listOf("foo", "bar"),
            listOf(consumerClassify(SI.Foo(1)), consumerClassify(SI.Bar)),
        )
    }
}
