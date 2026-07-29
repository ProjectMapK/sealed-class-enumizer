package io.github.projectmapk.fixtures.reentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

// 遅延初期化と初期化再入の JVM 実測固定（docs/test/ケース01-生成と実行時API.md §12）。
//
// 初期化再入（API-45）: kind の初期化子からの entries / valueOf / valueOfOrNull 参照は
// docs/概要.md §6 の禁止事項だが、JVM 実測では SOE にならず正常完了する。Kotlin object は INSTANCE を
// コンストラクタ先頭で代入するため、再入した initializer（2 回目）が有限で完了し（要素も非 null）、
// 外側の initializer の結果が上書きで確定する。副作用として初期化中に観測した entries は最終的な
// entries と別の List インスタンスになる（内容は同一・以降の memoization は正常）。禁止事項自体は不変
// （プラットフォームにより未定義動作。Native / Wasm は process-fatal になりうるため JVM でのみ実測）。
// 機構: runtime-api EnumishEntriesHolder の by lazy(SYNCHRONIZED) は同一スレッド再入を検出しない。
class ReentryTest {
    // docs/test/ケース01-生成と実行時API.md API-44: entries は初回アクセスまで末端未初期化・
    // 以降再構築なし（InitProbe 観測）
    @Test
    fun entriesAreBuiltLazily() {
        // 基底クラス・生成 Companion への参照では kind（末端 object）の初期化は走らない
        val companion = LazyRoot.Enumish
        assertEquals(emptyList(), InitProbe.events)
        // 初回アクセスで createEntries が走り、末端 L1 が初期化される
        companion.entries
        assertEquals(listOf("L1"), InitProbe.events)
        // 2 回目以降は再構築されない
        companion.entries
        assertEquals(listOf("L1"), InitProbe.events)
    }

    // docs/test/ケース01-生成と実行時API.md API-45: kind 初期化子からの entries / valueOf /
    // valueOfOrNull 参照（3 経路の独立階層）は JVM 実測で SOE せず二重実行完了する
    @Test
    fun initializerReentryCompletesTwice() {
        // entries 経路: 確定後は memoization により同一インスタンスで安定し、
        // 初期化中に観測した List は内容同一の別インスタンス（二重実行の痕跡）
        val entries = ReEntries.Enumish.entries
        assertEquals(listOf("Boom"), entries.map { it.label })
        assertSame(entries, ReEntries.Enumish.entries)
        assertEquals(listOf("Boom"), ReEntries.Boom.seenDuringInit.map { it.label })
        assertNotSame(entries, ReEntries.Boom.seenDuringInit)

        // valueOf 経路: 再入した valueOf は初期化途中の kind を解決して完了する
        assertEquals(listOf("Leaf"), ReValueOf.Enumish.entries.map { it.label })
        assertSame(ReValueOf.Leaf.Companion, ReValueOf.Leaf.eager)

        // valueOfOrNull 経路: 同様に完了する
        assertEquals(listOf("Leaf"), ReValueOfOrNull.Enumish.entries.map { it.label })
        assertSame(ReValueOfOrNull.Leaf.Companion, ReValueOfOrNull.Leaf.eager)
    }

    // docs/test/ケース01-生成と実行時API.md API-46: asEnumish / label / enumishCompanion は
    // EntriesHolder の lazy に触れないため初期化中も安全（非再入 API の境界）
    @Test
    fun nonLazyApisAreSafeDuringInit() {
        assertEquals("Probe", SafeInit.Probe.initLabel)
        assertSame(SafeInit.Enumish, SafeInit.Probe.initCompanion)
        assertEquals(listOf("Probe"), SafeInit.Enumish.entries.map { it.label })
    }
}
