package org.wrongwrong.fixtures.reentry

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

// entries の遅延初期化と初期化再入の box / 実行時負テスト
// （docs/テストケース管理.md TC-BOX-003 / TC-BOX-052 / TC-BOX-053 / TC-BOX-070 / TC-BOX-069）
//
// NG（TC-BOX-052/053/070）: docs は kind の初期化子からの entries / valueOf / valueOfOrNull 参照が
// 「lazy の同一スレッド再入により StackOverflowError」になるとするが、JVM 実測では正常完了する。
// Kotlin object は INSTANCE をコンストラクタ先頭で代入するため、再入した initializer（2 回目）が
// 有限で完了し（要素も非 null）、外側の initializer の結果が上書きで確定する。副作用として
// 初期化中に観測した entries は最終的な entries と別の List インスタンスになる（内容は同一・
// 以降の memoization は正常）。実測: outer=[Boom] inner=[Boom] sameInstance=false。
// 推定原因: runtime-api EnumishEntriesHolder の by lazy(SYNCHRONIZED) は同一スレッド再入を
// 検出しない（LazyThreadSafetyMode の仕様）。docs/修正方針案.md 反映待ち
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

    // TC-BOX-052: kind（末端 object）の初期化子から entries を参照すると遅延初期化の再入で StackOverflowError
    @Ignore("NG: 再入は SOE にならず正常完了する（lazy 再実行 + JVM の同一スレッド初期化再入許容） — docs/修正方針案.md 反映待ち")
    @Test
    fun entriesAccessFromKindInitializerOverflows() {
        assertFailsWith<StackOverflowError> { ReEntries.Enumish.entries }
    }

    // TC-BOX-053: companion（kind）の初期化子から valueOf を参照しても同様に再入で StackOverflowError
    @Ignore("NG: 再入は SOE にならず正常完了する（lazy 再実行 + JVM の同一スレッド初期化再入許容） — docs/修正方針案.md 反映待ち")
    @Test
    fun valueOfFromCompanionInitializerOverflows() {
        assertFailsWith<StackOverflowError> { ReValueOf.Enumish.entries }
    }

    // TC-BOX-070: valueOfOrNull も entries を経由するため同様に StackOverflowError
    @Ignore("NG: 再入は SOE にならず正常完了する（lazy 再実行 + JVM の同一スレッド初期化再入許容） — docs/修正方針案.md 反映待ち")
    @Test
    fun valueOfOrNullFromCompanionInitializerOverflows() {
        assertFailsWith<StackOverflowError> { ReValueOfOrNull.Enumish.entries }
    }

    // TC-BOX-069: asEnumish / label / enumishCompanion は EntriesHolder の lazy に触れないため
    // 初期化子から参照しても安全（非発火 near-miss の境界）
    @Test
    fun asEnumishAndLabelAreSafeDuringInitialization() {
        assertEquals("Probe", SafeInit.Probe.initLabel)
        assertSame(SafeInit.Enumish, SafeInit.Probe.initCompanion)
        assertEquals(listOf("Probe"), SafeInit.Enumish.entries.map { it.label })
    }
}
