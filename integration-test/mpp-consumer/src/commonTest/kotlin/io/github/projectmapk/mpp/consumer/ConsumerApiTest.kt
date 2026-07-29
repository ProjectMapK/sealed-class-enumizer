package io.github.projectmapk.mpp.consumer

import io.github.projectmapk.mpp.fixtures.EmptyRoot
import io.github.projectmapk.mpp.fixtures.SI
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

// 跨モジュール × MPP: プラグイン未適用モジュールの共通コードから mpp-producer の生成 API を
// 参照して各 platform で動作することの box テスト（docs/test/ケース05-境界横断.md
// XMP-37/XMP-08）
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

    // XMP-37: 失敗メッセージは跨モジュールでも全プラットフォーム同一文言
    @Test
    fun valueOfFailureMessageIsUniform() {
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("NoSuch") }
        assertEquals("No enumish entry with label 'NoSuch' in SI", failure.message)
    }

    // XMP-08: 空階層の跨モジュール / MPP 観測（entries = []・valueOf は常に失敗）
    @Test
    fun emptyHierarchyIsObservableAcrossModules() {
        assertEquals(emptyList(), EmptyRoot.Enumish.entries)
        val failure = assertFailsWith<IllegalArgumentException> { EmptyRoot.Enumish.valueOf("Any") }
        assertEquals("No enumish entry with label 'Any' in EmptyRoot", failure.message)
    }

    // 末端 class の kind を companion 名で参照する（docs/概要.md §1）。共通ソース由来の
    // 生成 companion がメタデータ / klib のネスト分類子として跨モジュールで解決できる
    @Test
    fun generatedCompanionIsNameable() {
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        assertEquals(
            listOf("foo", "bar"),
            listOf(consumerClassify(SI.Foo(1)), consumerClassify(SI.Bar)),
        )
    }
}
