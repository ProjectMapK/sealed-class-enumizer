package io.github.projectmapk.mpp.fixtures.jvm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// kind 初期化子からの entries 再入（docs/test/ケース05-境界横断.md XMP-47・docs/概要.md §6
// 「初期化再入の禁止」）。JVM 実測では例外は発生しない: lazy(SYNCHRONIZED) は同一スレッド再入可能で、
// JVM のクラス初期化再入により初期化途中の Boom が内側の計算から見えるため entries=[Boom]・
// Boom.observed=1・以降のアクセスも同一インスタンスで安定する（初期化子が二重実行されるだけに留まる）。
// 禁止事項自体は不変（プラットフォームにより未定義動作）。Native / Wasm は process-fatal になりうるため
// JVM でのみ実測する。
class ReentrantInitTest {
    @Test
    fun reentrantInitializerDoesNotOverflow() {
        val entries = Reentrant.Enumish.entries
        assertEquals(listOf("Boom"), entries.map { it.label })
        assertSame(entries, Reentrant.Enumish.entries)
        // 再入した初期化子が観測した entries は空でない（size 1）
        assertEquals(1, Reentrant.Boom.observed)
    }
}
