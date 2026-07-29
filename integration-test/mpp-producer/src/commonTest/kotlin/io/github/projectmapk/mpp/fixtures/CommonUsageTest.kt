package io.github.projectmapk.mpp.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

// commonMain の利用関数（CommonUsage.kt）の box テスト（docs/test/ケース05-境界横断.md
// XMP-39/XMP-49）。共通コードが metadata で解決され、各 platform の IR 充填で
// 同一結果になることを検証する
class CommonUsageTest {
    // XMP-39: 共通コードから entries / label を参照して各 platform で同一結果
    @Test
    fun commonFunctionSeesGeneratedApi() {
        assertEquals(listOf("Bar", "Foo"), pickLabels())
    }

    // XMP-39: commonMain に書いた else 無し kind-when が全 platform で動作する
    @Test
    fun commonKindWhenClassifiesWithoutElse() {
        assertEquals(listOf("foo", "bar"), listOf(classify(SI.Foo(1)), classify(SI.Bar)))
    }

    // XMP-39: enumishCompanion の default 実装ボディが各 platform で充填され動作する
    @Test
    fun enumishCompanionBodyIsFilledPerPlatform() {
        assertSame(SI.Enumish.entries, entriesVia(SI.Bar))
    }

    // XMP-49: 自作 reified ヘルパへの intrinsic 書き換えは行われない
    // （書き換えが有るなら TODO() に到達せず結果が返ってしまう）
    @Test
    fun selfMadeReifiedHelperIsNotRewritten() {
        assertFailsWith<NotImplementedError> { unresolvedHelper<SI.Enumish>() }
    }
}
