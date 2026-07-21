package org.wrongwrong.fixtures.reentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

// entries の遅延初期化と初期化再入の box テスト
// （docs/テストケース管理.md TC-BOX-003 / TC-BOX-052 / TC-BOX-053 / TC-BOX-070 / TC-BOX-069）
//
// 初期化再入（TC-BOX-052/053/070）: kind の初期化子からの entries / valueOf / valueOfOrNull 参照は
// docs/概要.md §6 の禁止事項だが、JVM 実測では SOE にならず正常完了する。Kotlin object は INSTANCE を
// コンストラクタ先頭で代入するため、再入した initializer（2 回目）が有限で完了し（要素も非 null）、
// 外側の initializer の結果が上書きで確定する。副作用として初期化中に観測した entries は最終的な
// entries と別の List インスタンスになる（内容は同一・以降の memoization は正常）。禁止事項自体は不変
// （プラットフォームにより未定義動作。Native / Wasm は process-fatal になりうるため JVM でのみ実測）。
// 機構: runtime-api EnumishEntriesHolder の by lazy(SYNCHRONIZED) は同一スレッド再入を検出しない。
class ReentryTest {
    // TC-BOX-003: entries は初回アクセス時に初めて構築される（クラス・Companion の参照だけでは末端は初期化されない）
    @Test
    fun entriesAreBuiltLazilyOnFirstAccess() {
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

    // TC-BOX-052: kind（末端 object）の初期化子から entries を参照しても SOE にならず、初期化子の
    // 二重実行で完了する。entries は [Boom] で確定し以降安定、初期化中に観測した List は別インスタンス
    @Test
    fun entriesAccessFromKindInitializerDoesNotOverflow() {
        val entries = ReEntries.Enumish.entries
        assertEquals(listOf("Boom"), entries.map { it.label })
        // 確定後は memoization により同一インスタンスで安定する
        assertSame(entries, ReEntries.Enumish.entries)
        // 二重実行の痕跡: 初期化中に観測した entries は内容同一だが確定後とは別インスタンス
        assertEquals(listOf("Boom"), ReEntries.Boom.seenDuringInit.map { it.label })
        assertNotSame(entries, ReEntries.Boom.seenDuringInit)
    }

    // TC-BOX-053: companion（kind）の初期化子から valueOf を参照しても同様に二重実行で完了し、
    // 再入した valueOf は初期化途中の kind を解決する
    @Test
    fun valueOfFromCompanionInitializerDoesNotOverflow() {
        val entries = ReValueOf.Enumish.entries
        assertEquals(listOf("Leaf"), entries.map { it.label })
        assertSame(entries, ReValueOf.Enumish.entries)
        assertSame(ReValueOf.Leaf.Companion, ReValueOf.Leaf.eager)
    }

    // TC-BOX-070: valueOfOrNull も entries を経由するため同様に二重実行で完了する
    @Test
    fun valueOfOrNullFromCompanionInitializerDoesNotOverflow() {
        val entries = ReValueOfOrNull.Enumish.entries
        assertEquals(listOf("Leaf"), entries.map { it.label })
        assertSame(entries, ReValueOfOrNull.Enumish.entries)
        assertSame(ReValueOfOrNull.Leaf.Companion, ReValueOfOrNull.Leaf.eager)
    }

    // TC-BOX-069: asEnumish / label / enumishCompanion は EntriesHolder の lazy に触れないため
    // 初期化子から参照しても再入せず安全（非発火 near-miss の境界）
    @Test
    fun asEnumishAndLabelAreSafeDuringInitialization() {
        assertEquals("Probe", SafeInit.Probe.initLabel)
        assertSame(SafeInit.Enumish, SafeInit.Probe.initCompanion)
        assertEquals(listOf("Probe"), SafeInit.Enumish.entries.map { it.label })
    }
}
