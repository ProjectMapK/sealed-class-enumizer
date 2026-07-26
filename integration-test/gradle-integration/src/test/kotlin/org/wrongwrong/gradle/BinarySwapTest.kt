package org.wrongwrong.gradle

import kotlin.io.path.readBytes
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// 旧バイナリ差し替え（docs/概要.md §7・docs/test/ケース06-ビルド動態.md BLD-40）:
// entries は「実行時に存在する版の集合」を返す（entries アクセサ非 inline の帰結）。
// 追加方向 = v1（2 末端）でコンパイルした consumer を v2（3 末端）jar で実行、
// 削除方向 = v2 でコンパイルした consumer2 を v1 jar で実行する
class BinarySwapTest {
    // docs/test/ケース06-ビルド動態.md BLD-40 追加方向: entries = 実行時集合・valueOf(新 label) 解決・
    // v1 網羅 when は NoWhenBranchMatchedException・呼出側定数プールに getEntries 残存（非 inline）
    @Test
    fun consumerCompiledAgainstV1SeesV2EntriesAtRuntime() {
        val dir = IcTestSupport.prepare("binary-swap", "swap-")

        // consumer は files() 依存のため、先に両版の jar を組み立ててからコンパイルする
        TestKitHarness.build(dir, ":producer-v1:jar", ":producer-v2:jar")
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        // v1 実行: コンパイル時と同じ 2 末端。valueOf("Baz") は失敗し、kind-when は全 kind を網羅
        val runV1 = TestKitHarness.build(dir, ":consumer:runV1")
        assertEquals(
            listOf("COUNT=2", "ENTRIES=Bar,Foo", "PROBE_BAZ=IAE", "WHEN=Bar->bar,Foo->foo"),
            IcTestSupport.outLines(runV1),
        )

        // v2 差し替え実行: consumer は再コンパイルされないまま entries が実行時の 3 末端を返し、
        // valueOf("Baz") が解決され、v1 網羅の when は Baz kind で NoWhenBranchMatchedException
        val runV2 = TestKitHarness.build(dir, ":consumer:runV2")
        assertEquals(TaskOutcome.UP_TO_DATE, runV2.task(":consumer:compileKotlin")?.outcome)
        assertEquals(
            listOf(
                "COUNT=3",
                "ENTRIES=Bar,Baz,Foo",
                "PROBE_BAZ=Baz",
                "WHEN=Bar->bar,Baz->NoWhenBranchMatchedException,Foo->foo",
            ),
            IcTestSupport.outLines(runV2),
        )

        // 非 inline 検証: getEntries のメンバー呼び出しが consumer の定数プールに残っていること
        // （inline なら v1 の集合が焼き込まれ差し替えが壊れる）
        val mainClass =
            dir.resolve("consumer/build/classes/kotlin/main/org/wrongwrong/swapuse/MainKt.class")
        val constantPoolText = String(mainClass.readBytes(), Charsets.ISO_8859_1)
        assertTrue("getEntries" in constantPoolText, "entries アクセサ呼び出しがメンバー参照のまま残ること")
    }

    // docs/test/ケース06-ビルド動態.md BLD-40 削除方向: v2 でコンパイルした consumer2 を v1 jar で
    // 実行すると entries = 2 件・消えた label の valueOf は実行時 IllegalArgumentException
    @Test
    fun consumerCompiledAgainstV2FailsValueOfOnV1Runtime() {
        val dir = IcTestSupport.prepare("binary-swap", "swap2-")

        TestKitHarness.build(dir, ":producer-v1:jar", ":producer-v2:jar")
        TestKitHarness.build(dir, ":consumer2:compileKotlin")

        // v2 実行（コンパイル時と同一版）: 3 末端の基準
        val runV2 = TestKitHarness.build(dir, ":consumer2:runV2")
        assertEquals(
            listOf(
                "COUNT=3",
                "ENTRIES=Bar,Baz,Foo",
                "PROBE_BAZ=Baz",
                "WHEN=Bar->bar,Baz->baz,Foo->foo",
            ),
            IcTestSupport.outLines(runV2),
        )

        // v1 差し替え実行: entries は実行時の 2 末端・valueOf("Baz") は実行時 IAE・
        // v2 網羅の when は存在する kind だけを通り全枝解決する
        val runV1 = TestKitHarness.build(dir, ":consumer2:runV1")
        assertEquals(TaskOutcome.UP_TO_DATE, runV1.task(":consumer2:compileKotlin")?.outcome)
        assertEquals(
            listOf("COUNT=2", "ENTRIES=Bar,Foo", "PROBE_BAZ=IAE", "WHEN=Bar->bar,Foo->foo"),
            IcTestSupport.outLines(runV1),
        )
    }
}
