package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.fail

// 全 API 表面の box テスト（docs/テストケース管理.md TC-MPP-001〜005/007/008/010/011/013/014/022/061）。
// commonTest に置くことで同一のテストが jvm / js / wasmJs / wasmWasi / native(host) の
// 各ターゲットで実行され、観測結果がターゲット間で割れないこと（V8）を検証する
class SiApiSurfaceTest {
    // TC-MPP-005: クロスターゲット・ゴールデン一致の期待値（1 回の assertEquals で比較する）
    private data class EntriesSnapshot(
        val labels: List<String>,
        val simpleNames: List<String?>,
        val strings: List<String>,
    )

    // TC-MPP-005/008: label 列・enumizedClass.simpleName 列・toString 列が全ターゲットで
    // 同一の期待 data class に一致する（simpleName のみ使用 = no-reflection 原則の範囲）
    @Test
    fun goldenSnapshotMatchesOnEveryTarget() {
        val actual = EntriesSnapshot(
            labels = SI.Enumish.entries.map { it.label },
            simpleNames = SI.Enumish.entries.map { it.enumizedClass.simpleName },
            strings = SI.Enumish.entries.map { it.toString() },
        )
        val expected = EntriesSnapshot(
            labels = listOf("Bar", "Foo"),
            simpleNames = listOf("Bar", "Foo"),
            strings = listOf("Bar", "Foo"),
        )
        assertEquals(expected, actual)
    }

    // TC-MPP-001〜004/061/022: entries の内容・FQN 順・kind シングルトン性。
    // 末端 class の kind の名指し（SI.Foo.Companion）は別コンパイレーションから未解決になる
    // NG（metadata nested_class_name 欠落）のため、同一性は asEnumish() 経由で観測する
    @Test
    fun entriesAreKindSingletonsInFqnOrder() {
        val entries: List<SI.Enumish> = SI.Enumish.entries
        assertEquals(listOf("Bar", "Foo"), entries.map { it.label })
        assertSame(SI.Bar, entries[0])
        val foo: SI = SI.Foo(0)
        assertSame(foo.asEnumish(), entries[1])
    }

    // TC-MPP-026/027: 遅延初期化は初回のみ構築され、以降は同一インスタンスを返す
    // （Native 新MM / JS・Wasm シングルスレッドのいずれでも意味論は不変）
    @Test
    fun entriesReturnsSameInstanceOnRepeatedAccess() {
        assertSame(SI.Enumish.entries, SI.Enumish.entries)
    }

    // TC-MPP-013/014: valueOf / valueOfOrNull と失敗メッセージの全プラットフォーム同一文言
    @Test
    fun valueOfResolvesAndFailsWithUniformMessage() {
        val foo: SI = SI.Foo(0)
        assertSame(foo.asEnumish(), SI.Enumish.valueOf("Foo"))
        assertSame(SI.Bar, SI.Enumish.valueOfOrNull("Bar"))
        assertNull(SI.Enumish.valueOfOrNull("NoSuch"))
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("NoSuch") }
        assertEquals("No enumish entry with label 'NoSuch' in SI", failure.message)
    }

    // TC-MPP-007: 値が別でも asEnumish は同一 companion への参照（末端 object は自身）
    @Test
    fun asEnumishReturnsSameSingletonAcrossInstances() {
        assertNotEquals(SI.Foo(42), SI.Foo(43))
        val first: SI = SI.Foo(42)
        val second: SI = SI.Foo(43)
        assertSame(first.asEnumish(), second.asEnumish())
        val bar: SI = SI.Bar
        assertSame(SI.Bar, bar.asEnumish())
    }

    // TC-MPP-011: label 拡張プロパティ（コード生成不要）が全ターゲットで一致
    @Test
    fun labelExtensionMatchesKindLabel() {
        val si: SI = SI.Foo(1)
        assertEquals(listOf("Foo", "Foo"), listOf(si.label, si.asEnumish().label))
    }

    // TC-MPP-010: enumishCompanion の共変 override により具体型で型付く
    @Test
    fun enumishCompanionIsCovariantlyTyped() {
        val kind: SI.Enumish = SI.Bar
        val companion: SI.Enumish.Companion = kind.enumishCompanion
        val entries: List<SI.Enumish> = companion.entries
        assertSame(SI.Enumish.entries, entries)
        assertSame(SI.Enumish, companion)
    }

    // kind 単位の網羅 when（else 省略）。末端 class の kind 枝は `SI.Foo.Companion ->` と書く仕様
    // （docs/概要.md §1・§6）だが、共通ソース由来の生成 companion は別コンパイレーション
    // （commonTest を含む）から Unresolved reference 'Companion' になり枝が書けない。本来形:
    //     val branches = SI.Enumish.entries.map { kind ->
    //         when (kind) {
    //             SI.Foo.Companion -> "foo"
    //             SI.Bar -> "bar"
    //         }
    //     }
    //     assertEquals(listOf("bar", "foo"), branches)
    // 同一コンパイレーション内の同形は CommonUsage.classify（commonMain）が green で通る。
    // NG: 共通ソースの生成 companion が跨コンパイレーションで未解決（JVM: metadata
    // nested_class_name 欠落 / klib: Cannot access class）— docs/修正方針案.md 反映待ち。
    // kotlin.test.Ignore は共通コードでは message 引数を取れないため理由はこのコメントが持つ
    @Ignore
    @Test
    fun kindWhenIsExhaustiveWithoutElse() {
        fail("NG により無効化（本来形は上のコメントを参照）")
    }
}
